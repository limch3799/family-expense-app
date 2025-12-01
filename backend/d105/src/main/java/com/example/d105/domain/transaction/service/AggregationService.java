package com.example.d105.domain.transaction.service;

import com.example.d105.domain.account.entity.UserAccount;
import com.example.d105.domain.account.entity.UserCard;
import com.example.d105.domain.account.repository.UserAccountRepository;
import com.example.d105.domain.account.repository.UserCardRepository;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.tracking.entity.GroupTrackingCard;
import com.example.d105.domain.tracking.entity.MemberTrackingAccount;
import com.example.d105.domain.tracking.repository.GroupTrackingCardRepository;
import com.example.d105.domain.tracking.repository.MemberTrackingAccountRepository;
import com.example.d105.domain.transaction.entity.AccountTransaction;
import com.example.d105.domain.transaction.entity.CardTransaction;
import com.example.d105.domain.transaction.entity.Category;
import com.example.d105.domain.transaction.entity.Transaction;
import com.example.d105.domain.transaction.repository.AccountTransactionRepository;
import com.example.d105.domain.transaction.repository.CardTransactionRepository;
import com.example.d105.domain.transaction.repository.CategoryRepository;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import com.example.d105.domain.transaction.exception.TransactionServerException;
import com.example.d105.read.entity.UserDailySummary;
import com.example.d105.read.entity.UserMonthlyExpenseStats;
import com.example.d105.read.entity.UserMonthlyTotals;
import com.example.d105.read.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * d105_read 스키마의 개인/그룹 집계 테이블들을 관리하는 서비스
 * N+1 문제를 해결하여 성능 최적화된 버전
 * Spring Retry 적용으로 일시적 실패에 대한 자동 재시도 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    // d105 스키마 (읽기)
    private final TransactionRepository transactionRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final CategoryRepository categoryRepository;

    // 계좌/카드 정보
    private final UserAccountRepository userAccountRepository;
    private final UserCardRepository userCardRepository;

    // 그룹 및 연결 정보
    private final GroupMemberRepository groupMemberRepository;
    private final MemberTrackingAccountRepository memberTrackingAccountRepository;
    private final GroupTrackingCardRepository groupTrackingCardRepository;

    // d105_read 스키마 (쓰기) - 개인 관련
    private final UserMonthlyExpenseStatsRepository userMonthlyExpenseStatsRepository;
    private final UserMonthlyTotalsRepository userMonthlyTotalsRepository;
    private final PartitionManagerRepository partitionManagerRepository;
    private final UserDailySummaryRepository userDailySummaryRepository;

    // 그룹 관련 Repository
    private final GroupDailySummaryRepository groupDailySummaryRepository;
    private final GroupMonthlyExpenseStatsRepository groupMonthlyExpenseStatsRepository;
    private final GroupMemberMonthlyExpenseStatsRepository groupMemberMonthlyExpenseStatsRepository;
    private final GroupMonthlyTotalsRepository groupMonthlyTotalsRepository;

    private final ObjectMapper objectMapper;

    /**
     * 연결된 계좌/카드만 고려한 개인 월별 지출 통계 업데이트
     * 재시도 가능한 메서드
     */
    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void updateUserMonthlyExpenseStats(Long userId, String yearMonth, List<Long> transactionIds) {
        log.debug("Updating user monthly expense stats for userId={}, yearMonth={}", userId, yearMonth);

        try {
            // 파티션 생성 확인
            ensurePartitionsExist(yearMonth);

            // 연결된 계좌/카드의 거래내역만 조회 (최적화된 버전 사용)
            List<Transaction> connectedTransactions = getConnectedTransactionsForMonthOptimized(userId, yearMonth);

            // 지출 거래만 필터링 (최적화된 버전 사용)
            List<Transaction> expenseTransactions = filterExpenseTransactionsOptimized(connectedTransactions);

            if (expenseTransactions.isEmpty()) {
                log.debug("No connected expense transactions found for userId={}, yearMonth={}", userId, yearMonth);
                clearUserExpenseStats(userId, yearMonth);
                return;
            }

            // 카테고리별 집계 (최적화된 버전 사용)
            Map<Short, CategoryExpenseData> categoryStats = calculateCategoryExpenseStatsOptimized(expenseTransactions);

            // 월별 총 지출액 계산
            long totalExpense = categoryStats.values().stream()
                    .mapToLong(CategoryExpenseData::getAmount)
                    .sum();

            // 카테고리별 통계 저장
            for (Map.Entry<Short, CategoryExpenseData> entry : categoryStats.entrySet()) {
                Short categoryId = entry.getKey();
                CategoryExpenseData data = entry.getValue();

                double percentage = totalExpense > 0 ?
                        (double) data.getAmount() / totalExpense * 100.0 : 0.0;

                String transactionIdsJson = objectMapper.writeValueAsString(data.getTransactionIds());

                userMonthlyExpenseStatsRepository.upsertUserMonthlyExpenseStats(
                        userId, yearMonth, categoryId, data.getCategoryName(),
                        data.getAmount(), data.getTransactionCount(),
                        Math.round(percentage * 100.0) / 100.0,
                        transactionIdsJson
                );
            }

            log.debug("Updated {} category stats for userId={}, yearMonth={} (optimized)",
                    categoryStats.size(), userId, yearMonth);

        } catch (JsonProcessingException e) {
            log.error("JSON processing failed for userId={}, yearMonth={}: {}", userId, yearMonth, e.getMessage(), e);
            throw TransactionServerException.databaseError(e);
        } catch (Exception e) {
            log.error("Failed to update user monthly expense stats for userId={}, yearMonth={}: {}",
                    userId, yearMonth, e.getMessage(), e);
            throw TransactionServerException.aggregationFailed(e);
        }
    }

    /**
     * 연결된 계좌/카드의 개인 월별 총계 업데이트 (최적화된 버전)
     */
    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void updateUserMonthlyTotals(Long userId, String yearMonth) {
        log.debug("Updating user monthly totals for userId={}, yearMonth={}", userId, yearMonth);

        try {
            ensurePartitionsExist(yearMonth);

            // 최적화된 버전 사용
            List<Transaction> connectedTransactions = getConnectedTransactionsForMonthOptimized(userId, yearMonth);
            List<Transaction> expenseTransactions = filterExpenseTransactionsOptimized(connectedTransactions);

            long totalAmount = expenseTransactions.stream()
                    .mapToLong(Transaction::getAmount)
                    .sum();

            int transactionCount = expenseTransactions.size();

            // 미분류 금액 계산 (배치 조회로 최적화)
            long uncategorizedAmount = calculateUncategorizedAmountOptimized(expenseTransactions);

            userMonthlyTotalsRepository.upsertUserMonthlyTotals(
                    userId, yearMonth, totalAmount, transactionCount, uncategorizedAmount
            );

            log.debug("Updated monthly totals for userId={}, yearMonth={}: amount={}, count={} (optimized)",
                    userId, yearMonth, totalAmount, transactionCount);

        } catch (Exception e) {
            log.error("Failed to update user monthly totals for userId={}, yearMonth={}: {}",
                    userId, yearMonth, e.getMessage(), e);
            throw TransactionServerException.aggregationFailed(e);
        }
    }

//    /**
//     * 특정 날짜들의 사용자 일별 요약 업데이트 (최적화된 버전)
//     */
//    @Transactional("readTransactionManager")
//    @Retryable(
//            value = {TransactionServerException.class, Exception.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2)
//    )
//    public void updateUserDailySummary(Long userId, Set<LocalDate> dates) {
//        log.debug("Updating user daily summary for userId={}, dates count={}", userId, dates.size());
//
//        try {
//            for (LocalDate date : dates) {
//                String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
//                ensurePartitionsExist(yearMonth);
//
//                // 최적화된 버전 사용
//                List<Transaction> connectedTransactions = getConnectedTransactionsForDateOptimized(userId, date);
//                List<Transaction> expenseTransactions = filterExpenseTransactionsOptimized(connectedTransactions);
//
//                if (expenseTransactions.isEmpty()) {
//                    log.debug("No connected expense transactions for userId={}, date={}", userId, date);
//                    clearUserDailySummary(userId, date, yearMonth);
//                    continue;
//                }
//
//                long totalAmount = expenseTransactions.stream()
//                        .mapToLong(Transaction::getAmount)
//                        .sum();
//
//                int transactionCount = expenseTransactions.size();
//
//                List<Long> transactionIds = expenseTransactions.stream()
//                        .map(Transaction::getTransactionId)
//                        .collect(Collectors.toList());
//
//                String transactionIdsJson = objectMapper.writeValueAsString(transactionIds);
//
//                userDailySummaryRepository.upsertUserDailySummary(
//                        userId, yearMonth, date, totalAmount, transactionCount, transactionIdsJson);
//
//                log.debug("Updated daily summary: userId={}, date={}, amount={}, count={}",
//                        userId, date, totalAmount, transactionCount);
//            }
//
//        } catch (JsonProcessingException e) {
//            log.error("JSON processing failed for userId={}, dates={}: {}", userId, dates.size(), e.getMessage(), e);
//            throw TransactionServerException.databaseError(e);
//        } catch (Exception e) {
//            log.error("Failed to update user daily summary for userId={}, dates count={}: {}",
//                    userId, dates.size(), e.getMessage(), e);
//            throw TransactionServerException.aggregationFailed(e);
//        }
//    }

    @Transactional("readTransactionManager")
    //@Retryable(...)
    public void updateUserDailySummary(Long userId, Set<LocalDate> dates) {
        log.info("=== Starting updateUserDailySummary for userId={}, dates={} ===", userId, dates);

        try {
            for (LocalDate date : dates) {
                String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                log.info("Processing date={}, yearMonth={} for userId={}", date, yearMonth, userId);

                ensurePartitionsExist(yearMonth);
                log.debug("Partitions ensured for yearMonth={}", yearMonth);

                // 최적화된 버전 사용
                List<Transaction> connectedTransactions = getConnectedTransactionsForDateOptimized(userId, date);
                log.info("Found {} connected transactions for userId={}, date={}",
                        connectedTransactions.size(), userId, date);

                List<Transaction> expenseTransactions = filterExpenseTransactionsOptimized(connectedTransactions);
                log.info("Filtered to {} expense transactions for userId={}, date={}",
                        expenseTransactions.size(), userId, date);

                if (expenseTransactions.isEmpty()) {
                    log.warn("No connected expense transactions for userId={}, date={} - clearing summary",
                            userId, date);
                    clearUserDailySummary(userId, date, yearMonth);
                    continue;
                }

                long totalAmount = expenseTransactions.stream()
                        .mapToLong(Transaction::getAmount)
                        .sum();

                int transactionCount = expenseTransactions.size();

                List<Long> transactionIds = expenseTransactions.stream()
                        .map(Transaction::getTransactionId)
                        .collect(Collectors.toList());

                log.info("Calculated summary for userId={}, date={}: amount={}, count={}, txIds={}",
                        userId, date, totalAmount, transactionCount, transactionIds);

                String transactionIdsJson = objectMapper.writeValueAsString(transactionIds);
                log.debug("Serialized transaction IDs to JSON: {}", transactionIdsJson);

                userDailySummaryRepository.upsertUserDailySummary(
                        userId, yearMonth, date, totalAmount, transactionCount, transactionIdsJson);

                log.info("✅ Successfully updated daily summary: userId={}, date={}, amount={}, count={}",
                        userId, date, totalAmount, transactionCount);
            }

        } catch (JsonProcessingException e) {
            log.error("❌ JSON processing failed for userId={}, dates={}: {}", userId, dates.size(), e.getMessage(), e);
            throw TransactionServerException.databaseError(e);
        } catch (Exception e) {
            log.error("❌ Failed to update user daily summary for userId={}, dates count={}: {}",
                    userId, dates.size(), e.getMessage(), e);
            throw TransactionServerException.aggregationFailed(e);
        }

        log.info("=== Completed updateUserDailySummary for userId={} ===", userId);
    }

    /**
     * 사용자의 모든 그룹에 대해 집계 업데이트 (최적화된 버전)
     */
    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void updateGroupAggregations(Long userId, String yearMonth) {
        log.debug("Updating group aggregations for userId={}, yearMonth={}", userId, yearMonth);

        try {
            List<GroupMember> userGroups = groupMemberRepository.findByUserIdAndMemberStatus(userId);

            if (userGroups.isEmpty()) {
                log.debug("User {} has no active group memberships", userId);
                return;
            }

            for (GroupMember groupMember : userGroups) {
                Long groupId = groupMember.getGroup().getGroupId();
                ensurePartitionsExist(yearMonth);

                // 모든 그룹 관련 업데이트는 최적화된 버전 사용
                updateGroupMonthlyStatsOptimized(groupId, yearMonth);
                updateGroupMemberMonthlyStatsOptimized(groupId, yearMonth);
                updateGroupMonthlyTotalsOptimized(groupId, yearMonth);
                updateGroupDailySummaryForMonthOptimized(groupId, yearMonth);

                log.debug("Updated group aggregations for groupId={}, yearMonth={} (optimized)", groupId, yearMonth);
            }

        } catch (Exception e) {
            log.error("Failed to update group aggregations for userId={}, yearMonth={}: {}",
                    userId, yearMonth, e.getMessage(), e);
            throw TransactionServerException.aggregationFailed(e);
        }
    }

    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void recalculateAllDailySummariesForUser(Long userId) {
        log.info("Starting full daily summary recalculation for userId={}", userId);

        LocalDate firstTransactionDate = transactionRepository.findFirstTransactionDateByUserId(userId);
        if (firstTransactionDate == null) {
            log.debug("No transactions found for userId={}", userId);
            return;
        }

        Set<LocalDate> allDates = new HashSet<>();
        LocalDate current = firstTransactionDate;
        LocalDate now = LocalDate.now();
        while (!current.isAfter(now)) {
            allDates.add(current);
            current = current.plusDays(1);
        }

        updateUserDailySummary(userId, allDates);

        log.info("Completed daily summary recalculation for userId={}: {} days processed",
                userId, allDates.size());
    }

    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void recalculateAllMonthsForUser(Long userId) {
        log.info("Starting full recalculation for userId={}", userId);

        try {
            LocalDate firstTransactionDate = transactionRepository.findFirstTransactionDateByUserId(userId);
            if (firstTransactionDate == null) {
                log.debug("No transactions found for userId={}", userId);
                return;
            }

            LocalDate current = firstTransactionDate.withDayOfMonth(1);
            LocalDate now = LocalDate.now().withDayOfMonth(1);

            int monthsRecalculated = 0;
            while (!current.isAfter(now)) {
                String yearMonth = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                updateUserMonthlyExpenseStats(userId, yearMonth, List.of());
                updateUserMonthlyTotals(userId, yearMonth);
                monthsRecalculated++;
                log.debug("Recalculated yearMonth={} for userId={}", yearMonth, userId);

                current = current.plusMonths(1);
            }

            log.info("Completed recalculation for userId={}: {} months processed", userId, monthsRecalculated);

        } catch (Exception e) {
            log.error("Failed to recalculate all months for userId={}: {}", userId, e.getMessage(), e);
            throw TransactionServerException.aggregationFailed(e);
        }
    }

    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void updateGroupDailySummary(Long userId, Set<LocalDate> dates) {
        log.debug("Updating group daily summary for userId={}, dates count={}", userId, dates.size());
        List<GroupMember> userGroups = groupMemberRepository.findByUserIdAndMemberStatus(userId);

        for (GroupMember groupMember : userGroups) {
            Long groupId = groupMember.getGroup().getGroupId();
            for (LocalDate date : dates) {
                String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                ensurePartitionsExist(yearMonth);
                updateGroupDailySummaryForDateOptimized(groupId, date, yearMonth);
            }
        }
    }

    @Transactional("readTransactionManager")
    @Retryable(
            value = {TransactionServerException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void recalculateAllGroupsForUser(Long userId) {
        log.info("Starting full group recalculation for userId={}", userId);
        List<GroupMember> userGroups = groupMemberRepository.findByUserIdAndMemberStatus(userId);
        for (GroupMember groupMember : userGroups) {
            Long groupId = groupMember.getGroup().getGroupId();
            recalculateAllMonthsForGroupOptimized(groupId);
        }
        log.info("Completed group recalculation for userId={}: {} groups processed", userId, userGroups.size());
    }

    @Recover
    public void recover(TransactionServerException ex, Object... args) {
        String methodInfo = args.length > 0 ?
                String.format("userId=%s", args[0]) : "unknown method";

        log.error("CRITICAL: Aggregation permanently failed after all retries. Manual intervention required. " +
                "Method: {}, Error: {}", methodInfo, ex.getMessage(), ex);

        throw TransactionServerException.aggregationFailed(ex);
    }

    // === 최적화된 Private Helper Methods ===

    private void recalculateAllMonthsForGroupOptimized(Long groupId) {
        try {
            List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdAndMemberStatus(groupId);

            LocalDate earliestDate = null;
            for (GroupMember member : groupMembers) {
                LocalDate firstDate = transactionRepository
                        .findFirstTransactionDateByUserId(member.getUser().getUserId());
                if (firstDate != null && (earliestDate == null || firstDate.isBefore(earliestDate))) {
                    earliestDate = firstDate;
                }
            }
            if (earliestDate == null) {
                return;
            }

            LocalDate current = earliestDate.withDayOfMonth(1);
            LocalDate now = LocalDate.now().withDayOfMonth(1);

            while (!current.isAfter(now)) {
                String yearMonth = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                try {
                    ensurePartitionsExist(yearMonth);
                    updateGroupMonthlyStatsOptimized(groupId, yearMonth);
                    updateGroupMemberMonthlyStatsOptimized(groupId, yearMonth);
                    updateGroupMonthlyTotalsOptimized(groupId, yearMonth);
                    updateGroupDailySummaryForMonthOptimized(groupId, yearMonth);
                    log.debug("Recalculated yearMonth={} for groupId={} (optimized)", yearMonth, groupId);
                } catch (Exception e) {
                    log.error("Failed to recalc groupId={}, yearMonth={}: {}", groupId, yearMonth, e.getMessage(), e);
                }
                current = current.plusMonths(1);
            }
        } catch (Exception e) {
            log.error("Failed to recalc all months for groupId={}: {}", groupId, e.getMessage(), e);
        }
    }

    @Transactional("readTransactionManager")
    public void recalculateStatsAfterExclusion(Long userId, Long transactionId, String yearMonth) {
        log.debug("Recalculating after EXCLUSION: userId={}, txId={}, ym={}", userId, transactionId, yearMonth);
        ensurePartitionsExist(yearMonth);
        updateUserMonthlyExpenseStats(userId, yearMonth, List.of());
        updateUserMonthlyTotals(userId, yearMonth);
    }

    @Transactional("readTransactionManager")
    public void recalculateStatsAfterInclusion(Long userId, Long transactionId, String yearMonth) {
        log.debug("Recalculating after INCLUSION: userId={}, txId={}, ym={}", userId, transactionId, yearMonth);
        recalculateStatsAfterExclusion(userId, transactionId, yearMonth);
    }

    @Transactional("readTransactionManager")
    public void recalculateStatsAfterCategoryChange(Long userId, Long transactionId, String yearMonth,
                                                    Short oldCategoryId, Short newCategoryId) {
        log.debug("Recalculating after CATEGORY CHANGE: userId={}, txId={}, ym={}, {}->{}",
                userId, transactionId, yearMonth, oldCategoryId, newCategoryId);
        ensurePartitionsExist(yearMonth);
        updateUserMonthlyExpenseStats(userId, yearMonth, List.of());
        updateUserMonthlyTotals(userId, yearMonth);
    }

    private void ensurePartitionsExist(String yearMonth) {
        try {
            partitionManagerRepository.createMonthlyPartitionIfNotExists("user_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("user_monthly_totals", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("user_daily_summary", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_daily_summary", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_member_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_monthly_totals", yearMonth);
        } catch (Exception e) {
            log.error("Failed to create partitions for yearMonth={}: {}", yearMonth, e.getMessage(), e);
            throw TransactionServerException.databaseError(e);
        }
    }

    private List<Long> parseTransactionIds(String transactionIdsJson) {
        try {
            if (transactionIdsJson == null || transactionIdsJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(transactionIdsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            log.error("Failed to parse transaction IDs JSON: {}", e.getMessage(), e);
            throw TransactionServerException.databaseError(e);
        }
    }

    // === 최적화된 핵심 메소드들 ===

    /**
     * 최적화된 연결 거래 조회 (JOIN으로 N+1 해결)
     */
    private List<Transaction> getConnectedTransactionsForMonthOptimized(Long userId, String yearMonth) {
        Optional<GroupMember> activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId);
        if (activeMember.isEmpty()) {
            log.debug("No active group membership for userId={}", userId);
            return List.of();
        }

        Long memberId = activeMember.get().getMemberId();
        return transactionRepository.findConnectedTransactionsForMonth(userId, yearMonth, memberId);
    }

//    private List<Transaction> getConnectedTransactionsForDateOptimized(Long userId, LocalDate date) {
//        Optional<GroupMember> activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId);
//        if (activeMember.isEmpty()) {
//            return List.of();
//        }
//
//        Long memberId = activeMember.get().getMemberId();
//        return transactionRepository.findConnectedTransactionsForDate(userId, date, memberId);
//    }
    private List<Transaction> getConnectedTransactionsForDateOptimized(Long userId, LocalDate date) {
        log.debug("Getting connected transactions for userId={}, date={}", userId, date);

        Optional<GroupMember> activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId);
        if (activeMember.isEmpty()) {
            log.warn("No active group membership for userId={}", userId);
            return List.of();
        }

        Long memberId = activeMember.get().getMemberId();
        log.debug("Found active member: userId={}, memberId={}", userId, memberId);

        List<Transaction> result = transactionRepository.findConnectedTransactionsForDate(userId, date, memberId);
        log.info("Found {} connected transactions for userId={}, date={}, memberId={}",
                result.size(), userId, date, memberId);

        return result;
    }

    /**
     * 최적화된 지출 거래 필터링 (배치 조회로 N+1 해결)
     */
    private List<Transaction> filterExpenseTransactionsOptimized(List<Transaction> transactions) {
        List<Long> transactionIds = transactions.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());

        if (transactionIds.isEmpty()) {
            return List.of();
        }

        // 지출 거래인 계좌 거래 ID 수집 (배치 조회)
        Set<Long> expenseAccountTxIds = accountTransactionRepository
                .findExpenseTransactionsByIds(transactionIds).stream()
                .map(AccountTransaction::getTransactionId)
                .collect(Collectors.toSet());

        return transactions.stream()
                .filter(t -> "CARD".equals(t.getTransactionType()) ||
                        expenseAccountTxIds.contains(t.getTransactionId()))
                .collect(Collectors.toList());
    }

    /**
     * 최적화된 카테고리별 지출 통계 계산 (배치 조회로 N+1 해결)
     */
    private Map<Short, CategoryExpenseData> calculateCategoryExpenseStatsOptimized(List<Transaction> transactions) {
        Map<Long, Short> categoryMap = getTransactionCategoryMapBatch(transactions);
        Map<Short, CategoryExpenseData> categoryStats = new HashMap<>();

        for (Transaction transaction : transactions) {
            Short categoryId = categoryMap.getOrDefault(transaction.getTransactionId(), (short) 1);
            String categoryName = getCategoryName(categoryId);

            categoryStats.computeIfAbsent(categoryId, k -> new CategoryExpenseData(categoryName))
                    .addTransaction(transaction.getTransactionId(), transaction.getAmount());
        }

        return categoryStats;
    }

    /**
     * 최적화된 미분류 금액 계산 (배치 조회 사용)
     */
    private long calculateUncategorizedAmountOptimized(List<Transaction> expenseTransactions) {
        Map<Long, Short> categoryMap = getTransactionCategoryMapBatch(expenseTransactions);
        return expenseTransactions.stream()
                .filter(t -> categoryMap.getOrDefault(t.getTransactionId(), (short) 1) == 1)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    private Map<Long, Short> getTransactionCategoryMapBatch(List<Transaction> transactions) {
        List<Long> transactionIds = transactions.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());

        if (transactionIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, Short> categoryMap = new HashMap<>();

        List<AccountTransaction> accountTxs = accountTransactionRepository.findByTransactionIds(transactionIds);
        for (AccountTransaction at : accountTxs) {
            categoryMap.put(at.getTransactionId(), at.getCategoryId());
        }

        List<CardTransaction> cardTxs = cardTransactionRepository.findByTransactionIds(transactionIds);
        for (CardTransaction ct : cardTxs) {
            categoryMap.put(ct.getTransactionId(), ct.getCategoryId());
        }

        return categoryMap;
    }

    /**
     * 최적화된 그룹 월별 통계 업데이트 (배치 조회로 N+1 해결)
     */
    private void updateGroupMonthlyStatsOptimized(Long groupId, String yearMonth) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdAndMemberStatus(groupId);
        if (groupMembers.isEmpty()) {
            return;
        }

        List<Long> userIds = groupMembers.stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toList());

        List<UserMonthlyExpenseStats> allUserStats = userMonthlyExpenseStatsRepository
                .findByUserIdsAndYearMonth(userIds, yearMonth);

        Map<Short, GroupCategoryAggregation> categoryAggregations = new HashMap<>();

        for (UserMonthlyExpenseStats stat : allUserStats) {
            if (stat.getExpenseAmount() <= 0) continue;

            Short categoryId = stat.getCategoryId();
            GroupCategoryAggregation aggregation = categoryAggregations
                    .computeIfAbsent(categoryId, k -> new GroupCategoryAggregation(stat.getCategoryName()));

            aggregation.addMemberExpense(stat.getExpenseAmount(), stat.getTransactionCount());
        }

        long totalGroupExpense = categoryAggregations.values().stream()
                .mapToLong(GroupCategoryAggregation::getTotalAmount)
                .sum();

        for (Map.Entry<Short, GroupCategoryAggregation> entry : categoryAggregations.entrySet()) {
            Short categoryId = entry.getKey();
            GroupCategoryAggregation aggregation = entry.getValue();

            double percentage = totalGroupExpense > 0 ?
                    (double) aggregation.getTotalAmount() / totalGroupExpense * 100.0 : 0.0;

            groupMonthlyExpenseStatsRepository.upsertGroupMonthlyExpenseStats(
                    groupId, yearMonth, categoryId, aggregation.getCategoryName(),
                    aggregation.getTotalAmount(), aggregation.getTotalCount(),
                    aggregation.getMemberCount(), Math.round(percentage * 100.0) / 100.0
            );
        }
    }

    /**
     * 최적화된 그룹 멤버별 월별 통계 업데이트 (이미 배치 조회 적용됨)
     */
    private void updateGroupMemberMonthlyStatsOptimized(Long groupId, String yearMonth) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdAndMemberStatus(groupId);

        List<Long> userIds = groupMembers.stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toList());

        List<UserMonthlyExpenseStats> allUserStats = userMonthlyExpenseStatsRepository
                .findByUserIdsAndYearMonth(userIds, yearMonth);

        Map<Long, List<UserMonthlyExpenseStats>> statsByUser = allUserStats.stream()
                .collect(Collectors.groupingBy(UserMonthlyExpenseStats::getUserId));

        for (GroupMember member : groupMembers) {
            List<UserMonthlyExpenseStats> userStats = statsByUser.getOrDefault(
                    member.getUser().getUserId(), Collections.emptyList());

            for (UserMonthlyExpenseStats stat : userStats) {
                if (stat.getExpenseAmount() <= 0) continue;

                groupMemberMonthlyExpenseStatsRepository.upsertGroupMemberMonthlyExpenseStats(
                        groupId, member.getUser().getUserId(), yearMonth, stat.getCategoryId(),
                        stat.getCategoryName(), stat.getExpenseAmount(),
                        stat.getTransactionCount(), stat.getExpensePercentage().doubleValue()
                );
            }
        }
    }

    /**
     * 최적화된 그룹 월별 총합 업데이트 (배치 조회로 N+1 해결)
     */
    private void updateGroupMonthlyTotalsOptimized(Long groupId, String yearMonth) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdAndMemberStatus(groupId);

        List<Long> userIds = groupMembers.stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toList());

        // 배치로 모든 멤버의 월별 총계 조회
        List<UserMonthlyTotals> allUserTotals = userMonthlyTotalsRepository
                .findByUserIdsAndYearMonth(userIds, yearMonth);

        Map<Long, UserMonthlyTotals> totalsByUser = allUserTotals.stream()
                .collect(Collectors.toMap(UserMonthlyTotals::getUserId, total -> total));

        long totalAmount = 0;
        int activeMemberCount = groupMembers.size();

        for (GroupMember member : groupMembers) {
            UserMonthlyTotals userTotal = totalsByUser.get(member.getUser().getUserId());
            if (userTotal != null) {
                totalAmount += userTotal.getTotalAmount();
            }
        }

        long avgAmountPerMember = activeMemberCount > 0 ? totalAmount / activeMemberCount : 0;

        groupMonthlyTotalsRepository.upsertGroupMonthlyTotals(
                groupId, yearMonth, totalAmount, activeMemberCount, avgAmountPerMember
        );
    }

    /**
     * 최적화된 그룹 일별 요약 월별 업데이트
     */
    private void updateGroupDailySummaryForMonthOptimized(Long groupId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            updateGroupDailySummaryForDateOptimized(groupId, current, yearMonth);
            current = current.plusDays(1);
        }
    }

    /**
     * 최적화된 그룹 일별 요약 업데이트 (배치 조회로 N+1 해결)
     */
    private void updateGroupDailySummaryForDateOptimized(Long groupId, LocalDate date, String yearMonth) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdAndMemberStatus(groupId);

        List<Long> userIds = groupMembers.stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toList());

        // 배치로 모든 멤버의 일별 요약 조회
        List<UserDailySummary> allUserSummaries = userDailySummaryRepository
                .findByUserIdsAndDate(userIds, date);

        Map<Long, UserDailySummary> summaryByUserId = allUserSummaries.stream()
                .collect(Collectors.toMap(UserDailySummary::getUserId, summary -> summary));

        long totalAmount = 0;
        int totalCount = 0;

        for (GroupMember member : groupMembers) {
            UserDailySummary summary = summaryByUserId.get(member.getUser().getUserId());
            if (summary != null && summary.getSummaryDate().equals(date)) {
                totalAmount += summary.getTotalAmount();
                totalCount += summary.getTransactionCount();
            }
        }

        groupDailySummaryRepository.upsertGroupDailySummary(
                groupId, yearMonth, date, totalAmount, totalCount
        );
    }

    private String getCategoryName(Short categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("미분류");
    }

    private void clearUserExpenseStats(Long userId, String yearMonth) {
        log.debug("Clearing expense stats for userId={}, yearMonth={} (no connected accounts/cards)", userId, yearMonth);

        List<UserMonthlyExpenseStats> existingStats = userMonthlyExpenseStatsRepository.findByUserIdAndYearMonth(userId, yearMonth);
        for (UserMonthlyExpenseStats stat : existingStats) {
            userMonthlyExpenseStatsRepository.upsertUserMonthlyExpenseStats(
                    userId, yearMonth, stat.getCategoryId(), stat.getCategoryName(),
                    0L, 0, 0.0, "[]"
            );
        }

        userMonthlyTotalsRepository.upsertUserMonthlyTotals(userId, yearMonth, 0L, 0, 0L);
    }

    private void clearUserDailySummary(Long userId, LocalDate date, String yearMonth) {
        log.debug("Clearing daily summary for userId={}, date={} (no connected transactions)", userId, date);

        userDailySummaryRepository.upsertUserDailySummary(
                userId, yearMonth, date, 0L, 0, "[]");
    }

    // === Helper Classes ===

    private static class CategoryExpenseData {
        private final String categoryName;
        private long amount = 0;
        private int transactionCount = 0;
        private final List<Long> transactionIds = new ArrayList<>();

        public CategoryExpenseData(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addTransaction(Long transactionId, Integer amount) {
            this.transactionIds.add(transactionId);
            this.amount += amount;
            this.transactionCount++;
        }

        public String getCategoryName() { return categoryName; }
        public long getAmount() { return amount; }
        public int getTransactionCount() { return transactionCount; }
        public List<Long> getTransactionIds() { return transactionIds; }
    }

    private static class GroupCategoryAggregation {
        private final String categoryName;
        private long totalAmount = 0;
        private int totalCount = 0;
        private int memberCount = 0;

        public GroupCategoryAggregation(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addMemberExpense(long amount, int count) {
            this.totalAmount += amount;
            this.totalCount += count;
            this.memberCount++;
        }

        public String getCategoryName() { return categoryName; }
        public long getTotalAmount() { return totalAmount; }
        public int getTotalCount() { return totalCount; }
        public int getMemberCount() { return memberCount; }
    }
}