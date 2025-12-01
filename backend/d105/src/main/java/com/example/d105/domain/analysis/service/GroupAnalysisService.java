package com.example.d105.domain.analysis.service;

import com.example.d105.domain.analysis.dto.response.*;
import com.example.d105.domain.analysis.exception.AnalysisServerException;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.transaction.entity.AccountTransaction;
import com.example.d105.domain.transaction.entity.CardTransaction;
import com.example.d105.domain.transaction.entity.Category;
import com.example.d105.domain.transaction.entity.Transaction;
import com.example.d105.domain.transaction.repository.AccountTransactionRepository;
import com.example.d105.domain.transaction.repository.CardTransactionRepository;
import com.example.d105.domain.transaction.repository.CategoryRepository;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import com.example.d105.read.entity.GroupDailySummary;
import com.example.d105.read.entity.GroupMemberMonthlyExpenseStats;
import com.example.d105.read.entity.GroupMonthlyTotals;
import com.example.d105.read.entity.GroupMonthlyExpenseStats;
import com.example.d105.read.entity.UserMonthlyExpenseStats;
import com.example.d105.read.entity.UserDailySummary;
import com.example.d105.read.repository.*;
import com.example.d105.security.service.CryptoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupAnalysisService {

    private final GroupDailySummaryRepository groupDailySummaryRepository;
    private final UserDailySummaryRepository userDailySummaryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TransactionRepository transactionRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final GroupMonthlyTotalsRepository groupMonthlyTotalsRepository;
    private final GroupMonthlyExpenseStatsRepository groupMonthlyExpenseStatsRepository;
    private final GroupMemberMonthlyExpenseStatsRepository groupMemberMonthlyExpenseStatsRepository;
    private final UserMonthlyExpenseStatsRepository userMonthlyExpenseStatsRepository;
    private final GroupRepository groupRepository;
    private final CryptoService cryptoService;
    private final PartitionManagerRepository partitionManagerRepository;

    /**
     * 멤버 정보 (닉네임 + 실명)
     */
    @Getter
    @AllArgsConstructor
    private static class MemberInfo {
        private final String displayName;  // 그룹 닉네임
        private final String realName;     // 실명
    }

    /**
     * 최근 14일 간 지출 총액 변화 조회
     */
    @Transactional(readOnly = true)
    public GroupRecentTrendResponse getRecentTrend(Long userId, Long groupId) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(13); // 14일간 (오늘 포함)

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExistForDateRange(startDate, endDate);

        // GroupDailySummary에서 최근 14일 데이터 조회
        List<GroupDailySummary> summaries = groupDailySummaryRepository
                .findRecentDailySummaries(groupId, startDate);

        // 날짜별 맵 생성 (빠른 조회를 위해)
        Map<LocalDate, Long> summaryMap = summaries.stream()
                .collect(Collectors.toMap(
                        GroupDailySummary::getSummaryDate,
                        GroupDailySummary::getTotalAmount
                ));

        // 14일간 전체 날짜 생성 (데이터가 없는 날짜는 0으로 처리)
        List<GroupRecentTrendResponse.DailyAmount> dailyAmounts = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            Long amount = summaryMap.getOrDefault(current, 0L);

            dailyAmounts.add(GroupRecentTrendResponse.DailyAmount.builder()
                    .date(current.toString()) // "2025-09-01"
                    .totalAmount(amount)
                    .build());

            current = current.plusDays(1);
        }

        return GroupRecentTrendResponse.builder()
                .dailyAmounts(dailyAmounts)
                .build();
    }

    /**
     * 그룹 월간 일별 지출 총액 조회 (캘린더용)
     */
    @Transactional(readOnly = true)
    public GroupMonthlyCalendarResponse getMonthlyCalendar(Long userId, Long groupId, String yearMonth) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        // GroupDailySummary에서 해당 월의 모든 일별 데이터 조회
        List<GroupDailySummary> summaries = groupDailySummaryRepository
                .findByGroupIdAndYearMonthOrderBySummaryDateAsc(groupId, yearMonth);

        // 날짜별 맵 생성
        Map<LocalDate, GroupDailySummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(
                        GroupDailySummary::getSummaryDate,
                        summary -> summary
                ));

        // 해당 월의 모든 날짜 생성 (1일~말일)
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<GroupMonthlyCalendarResponse.CalendarDay> monthlyCalendar = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            GroupDailySummary summary = summaryMap.get(current);

            // 데이터가 없는 날은 0으로 설정
            Long totalAmount = (summary != null) ? summary.getTotalAmount() : 0L;
            Integer transactionCount = (summary != null) ? summary.getTransactionCount() : 0;

            monthlyCalendar.add(GroupMonthlyCalendarResponse.CalendarDay.builder()
                    .date(current.toString()) // "2025-09-01"
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .build());

            current = current.plusDays(1);
        }

        return GroupMonthlyCalendarResponse.builder()
                .monthlyCalendar(monthlyCalendar)
                .build();
    }

    /**
     * 특정 날짜 그룹 거래내역 조회
     */
//    @Transactional(readOnly = true)
//    public GroupDailyTransactionsResponse getDailyTransactions(Long userId, Long groupId, String dateStr) {
//        // ★ 권한 검증
//        validateGroupMembership(userId, groupId);
//
//        LocalDate date = LocalDate.parse(dateStr);
//        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
//
//        // ★ 파티션 확인/생성
//        ensureAnalysisPartitionsExist(yearMonth);
//
//        // 1. 그룹 멤버 목록 조회
//        List<GroupMember> groupMembers = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);
//
//        if (groupMembers.isEmpty()) {
//            return createEmptyDailyTransactionsResponse(groupId, dateStr);
//        }
//
//        // 2. 각 멤버의 해당 날짜 거래 ID 수집
//        List<Long> allTransactionIds = new ArrayList<>();
//
//        for (GroupMember member : groupMembers) {
//            List<UserDailySummary> userSummaries = userDailySummaryRepository
//                    .findRecentDailySummaries(member.getUser().getUserId(), date);
//
//            // 해당 날짜의 데이터만 필터링
//            for (UserDailySummary summary : userSummaries) {
//                if (summary.getSummaryDate().equals(date)) {
//                    List<Long> transactionIds = parseTransactionIds(summary.getTransactionIds());
//                    allTransactionIds.addAll(transactionIds);
//                    break; // 해당 날짜 찾으면 중단
//                }
//            }
//        }
//
//        if (allTransactionIds.isEmpty()) {
//            return createEmptyDailyTransactionsResponse(groupId, dateStr);
//        }
//
//        // 3. 거래 ID들로 상세 정보 조회
//        List<Transaction> transactions = transactionRepository.findBasicTransactionInfoByIds(allTransactionIds);
//
//        // 4. 그룹 멤버 정보를 맵으로 변환 (닉네임 + 실명)
//        Map<Long, MemberInfo> memberInfoMap = groupMembers.stream()
//                .collect(Collectors.toMap(
//                        member -> member.getUser().getUserId(),
//                        member -> new MemberInfo(member.getDisplayName(), cryptoService.decryptAES(member.getUser().getUsername()))
//                ));
//
//        // 5. 거래 상세 정보 생성
//        List<GroupDailyTransactionsResponse.GroupTransactionDetail> transactionDetails =
//                transactions.stream()
//                        .map(transaction -> convertToGroupTransactionDetail(transaction, memberInfoMap))
//                        .collect(Collectors.toList());
//
//        // 6. 총액과 건수 계산
//        Long totalAmount = transactions.stream()
//                .mapToLong(Transaction::getAmount)
//                .sum();
//
//        return GroupDailyTransactionsResponse.builder()
//                .date(dateStr)
//                .groupId(groupId)
//                .totalAmount(totalAmount)
//                .totalCount(transactions.size())
//                .transactions(transactionDetails)
//                .build();
//    }

    // GroupAnalysisService.java

    @Transactional(readOnly = true)
    public GroupDailyTransactionsResponse getDailyTransactions(Long userId, Long groupId, String dateStr) {
        // ★ 권한 검증 (기존 그대로)
        validateGroupMembership(userId, groupId);

        LocalDate date = LocalDate.parse(dateStr);

        // 1) 그룹 멤버 조회 (기존 그대로)
        List<GroupMember> groupMembers =
                groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);
        if (groupMembers.isEmpty()) {
            return createEmptyDailyTransactionsResponse(groupId, dateStr);
        }

        // 2) ★ 변경 포인트: UserDailySummary를 보지 말고,
        //    실거래에서 '연결된' 거래의 ID를 isExcluded 여부 무관하게 모두 모은다.
        List<Long> allTransactionIds = new ArrayList<>();
        for (GroupMember member : groupMembers) {
            Long memberUserId = member.getUser().getUserId();
            Long memberId = member.getMemberId(); // GroupMember PK 가정
            List<Long> ids = transactionRepository
                    .findConnectedTransactionIdsForDateIncludingExcluded(memberUserId, date, memberId);
            if (ids != null && !ids.isEmpty()) {
                allTransactionIds.addAll(ids);
            }
        }

        // 중복 제거
        allTransactionIds = allTransactionIds.stream().distinct().toList();

        if (allTransactionIds.isEmpty()) {
            return createEmptyDailyTransactionsResponse(groupId, dateStr);
        }

        // 3) 기존과 동일: ID로 기본 필드 한번에 로드
        List<Transaction> transactions =
                transactionRepository.findBasicTransactionInfoByIds(allTransactionIds);

        // 4) 기존과 동일: 멤버 정보 맵, DTO 매핑/합계 계산, 응답 생성
        Map<Long, MemberInfo> memberInfoMap = groupMembers.stream()
                .collect(Collectors.toMap(
                        m -> m.getUser().getUserId(),
                        m -> new MemberInfo(m.getDisplayName(), cryptoService.decryptAES(m.getUser().getUsername()))
                ));

        List<GroupDailyTransactionsResponse.GroupTransactionDetail> details = transactions.stream()
                .map(t -> convertToGroupTransactionDetail(t, memberInfoMap))
                .collect(Collectors.toList());

        Long totalAmount = transactions.stream().mapToLong(Transaction::getAmount).sum();

        return GroupDailyTransactionsResponse.builder()
                .date(dateStr)
                .groupId(groupId)
                .totalAmount(totalAmount)
                .totalCount(transactions.size())
                .transactions(details)
                .build();
    }


    /**
     * 그룹 총 지출액 및 증감률 조회
     */
    @Transactional(readOnly = true)
    public GroupTotalExpenseResponse getTotalExpense(Long userId, Long groupId, String yearMonth) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        // 1. 현재 월 집계
        Optional<GroupMonthlyTotals> currentMonth = groupMonthlyTotalsRepository
                .findByGroupIdAndYearMonth(groupId, yearMonth);

        long currentTotalAmount = currentMonth.map(GroupMonthlyTotals::getTotalAmount).orElse(0L);
        int currentTransactionCount = getTransactionCountForMonth(groupId, yearMonth);

        // 2. 이전 월 집계
        String previousYearMonth = getPreviousMonth(yearMonth);
        // ★ 이전 월 파티션도 확인/생성
        ensureAnalysisPartitionsExist(previousYearMonth);

        Optional<GroupMonthlyTotals> previousMonth = groupMonthlyTotalsRepository
                .findByGroupIdAndYearMonth(groupId, previousYearMonth);

        long previousTotalAmount = previousMonth.map(GroupMonthlyTotals::getTotalAmount).orElse(0L);

        // 3. 증감률 계산
        String changeRate;
        long changeAmount = currentTotalAmount - previousTotalAmount;
        String changeType = "INCREASE";

        if (previousTotalAmount == 0) {
            changeRate = "신규";
            changeAmount = currentTotalAmount;
        } else {
            double rate = ((double) changeAmount / previousTotalAmount) * 100.0;
            rate = Math.round(rate * 100.0) / 100.0;

            if (changeAmount < 0) {
                changeType = "DECREASE";
                changeAmount = Math.abs(changeAmount);
                rate = Math.abs(rate);
            }

            changeRate = (rate == Math.floor(rate)) ?
                    String.format("%.0f%%", rate) : String.format("%.2f%%", rate);
        }

        return GroupTotalExpenseResponse.builder()
                .currentPeriod(GroupTotalExpenseResponse.CurrentPeriod.builder()
                        .yearMonth(yearMonth)
                        .totalAmount(currentTotalAmount)
                        .transactionCount(currentTransactionCount)
                        .build())
                .previousPeriod(GroupTotalExpenseResponse.PreviousPeriod.builder()
                        .yearMonth(previousYearMonth)
                        .totalAmount(previousTotalAmount)
                        .build())
                .changeRate(changeRate)
                .changeAmount(changeAmount)
                .changeType(changeType)
                .build();
    }

    /**
     * 카테고리별 지출 분석
     */
    @Transactional(readOnly = true)
    public GroupCategoryAnalysisResponse getCategoryAnalysis(Long userId, Long groupId, String yearMonth) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        log.debug("Getting category analysis for groupId={}, yearMonth={}", groupId, yearMonth);

        // 그룹의 월별 카테고리별 지출 통계 조회
        List<GroupMonthlyExpenseStats> expenseStats = groupMonthlyExpenseStatsRepository
                .findByGroupIdAndYearMonth(groupId, yearMonth);

        if (expenseStats.isEmpty()) {
            return GroupCategoryAnalysisResponse.builder()
                    .yearMonth(yearMonth)
                    .totalAmount(0L)
                    .categoryExpenses(List.of())
                    .build();
        }

        // 총 지출액 계산
        Long totalAmount = expenseStats.stream()
                .mapToLong(GroupMonthlyExpenseStats::getTotalExpense)
                .sum();

        // 카테고리별 지출 정보 변환 (금액 기준 내림차순 정렬)
        List<GroupCategoryAnalysisResponse.CategoryExpense> categoryExpenses = expenseStats.stream()
                .filter(stat -> stat.getTotalExpense() > 0) // 금액이 0인 카테고리 제외
                .map(stat -> GroupCategoryAnalysisResponse.CategoryExpense.builder()
                        .categoryId(stat.getCategoryId())
                        .categoryName(stat.getCategoryName())
                        .amount(stat.getTotalExpense())
                        .percentage(stat.getExpensePercentage().doubleValue())
                        .transactionCount(stat.getTransactionCount())
                        .build())
                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount())) // 금액 기준 내림차순
                .collect(Collectors.toList());

        return GroupCategoryAnalysisResponse.builder()
                .yearMonth(yearMonth)
                .totalAmount(totalAmount)
                .categoryExpenses(categoryExpenses)
                .build();
    }

    /**
     * 카테고리별 그룹 거래내역 조회
     */
    @Transactional(readOnly = true)
    public GroupCategoryTransactionsResponse getCategoryTransactions(Long userId, Long groupId, String yearMonth, Short categoryId) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        log.debug("Getting category transactions for groupId={}, yearMonth={}, categoryId={}",
                groupId, yearMonth, categoryId);

        // 해당 카테고리에 지출한 멤버들 조회
        List<GroupMemberMonthlyExpenseStats> memberStats = groupMemberMonthlyExpenseStatsRepository
                .findByGroupIdAndYearMonthAndCategoryId(groupId, yearMonth, categoryId);

        if (memberStats.isEmpty()) {
            return createEmptyCategoryTransactionsResponse(yearMonth, categoryId);
        }

        String categoryName = memberStats.get(0).getCategoryName();
        Long totalAmount = memberStats.stream().mapToLong(GroupMemberMonthlyExpenseStats::getExpenseAmount).sum();
        Integer totalCount = memberStats.stream().mapToInt(GroupMemberMonthlyExpenseStats::getTransactionCount).sum();

        // 각 멤버별로 해당 카테고리의 거래내역 수집
        List<GroupCategoryTransactionsResponse.TransactionDetail> allTransactions = new ArrayList<>();
        Map<Long, MemberInfo> memberInfoMap = createMemberInfoMap(groupId);

        for (GroupMemberMonthlyExpenseStats memberStat : memberStats) {
            Long memberUserId = memberStat.getUserId();
            MemberInfo memberInfo = memberInfoMap.getOrDefault(memberUserId, new MemberInfo("알 수 없음", "알 수 없음"));

            List<UserMonthlyExpenseStats> userStats = userMonthlyExpenseStatsRepository
                    .findByUserIdAndYearMonthAndCategoryId(memberUserId, yearMonth, categoryId);

            for (UserMonthlyExpenseStats userStat : userStats) {
                List<Long> transactionIds = parseTransactionIds(userStat.getTransactionIds());

                if (!transactionIds.isEmpty()) {
                    List<Transaction> transactions = transactionRepository.findBasicTransactionInfoByIds(transactionIds);

                    for (Transaction transaction : transactions) {
                        GroupCategoryTransactionsResponse.TransactionDetail detail =
                                convertToTransactionDetail(transaction, memberInfo);
                        allTransactions.add(detail);
                    }
                }
            }
        }

        // 날짜/시간 기준 내림차순 정렬
        allTransactions.sort((a, b) -> {
            int dateCompare = b.getTransactionDate().compareTo(a.getTransactionDate());
            if (dateCompare != 0) return dateCompare;
            return b.getTransactionTime().compareTo(a.getTransactionTime());
        });

        return GroupCategoryTransactionsResponse.builder()
                .yearMonth(yearMonth)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .transactions(allTransactions)
                .build();
    }

    /**
     * 멤버별 지출 분석
     */
    @Transactional(readOnly = true)
    public GroupMemberAnalysisResponse getMemberAnalysis(Long userId, Long groupId, String yearMonth) {
        // ★ 권한 검증
        validateGroupMembership(userId, groupId);

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        log.debug("Getting member analysis for groupId={}, yearMonth={}", groupId, yearMonth);

        List<GroupMemberMonthlyExpenseStats> memberStats = groupMemberMonthlyExpenseStatsRepository
                .findByGroupIdAndYearMonth(groupId, yearMonth);

        if (memberStats.isEmpty()) {
            return GroupMemberAnalysisResponse.builder()
                    .yearMonth(yearMonth)
                    .memberAnalysis(List.of())
                    .build();
        }

        Map<Long, MemberInfo> memberInfoMap = createMemberInfoMap(groupId);
        Map<Long, List<GroupMemberMonthlyExpenseStats>> memberStatsMap = memberStats.stream()
                .filter(stat -> stat.getExpenseAmount() > 0)
                .collect(Collectors.groupingBy(GroupMemberMonthlyExpenseStats::getUserId));

        List<GroupMemberAnalysisResponse.MemberAnalysis> memberAnalysisList = new ArrayList<>();

        for (Map.Entry<Long, List<GroupMemberMonthlyExpenseStats>> entry : memberStatsMap.entrySet()) {
            Long memberUserId = entry.getKey();
            List<GroupMemberMonthlyExpenseStats> userStats = entry.getValue();
            MemberInfo memberInfo = memberInfoMap.getOrDefault(memberUserId, new MemberInfo("알 수 없음", "알 수 없음"));

            Long memberTotalAmount = userStats.stream().mapToLong(GroupMemberMonthlyExpenseStats::getExpenseAmount).sum();

            List<GroupMemberAnalysisResponse.CategoryBreakdown> categoryBreakdowns = userStats.stream()
                    .map(stat -> {
                        double percentage = memberTotalAmount > 0 ?
                                (double) stat.getExpenseAmount() / memberTotalAmount * 100.0 : 0.0;

                        return GroupMemberAnalysisResponse.CategoryBreakdown.builder()
                                .categoryId(stat.getCategoryId())
                                .categoryName(stat.getCategoryName())
                                .amount(stat.getExpenseAmount())
                                .percentage(Math.round(percentage * 100.0) / 100.0)
                                .build();
                    })
                    .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                    .collect(Collectors.toList());

            memberAnalysisList.add(GroupMemberAnalysisResponse.MemberAnalysis.builder()
                    .userId(memberUserId)
                    .memberName(memberInfo.getDisplayName())  // 그룹 닉네임만 사용
                    .totalAmount(memberTotalAmount)
                    .categoryBreakdown(categoryBreakdowns)
                    .build());
        }

        memberAnalysisList.sort((a, b) -> Long.compare(b.getTotalAmount(), a.getTotalAmount()));

        return GroupMemberAnalysisResponse.builder()
                .yearMonth(yearMonth)
                .memberAnalysis(memberAnalysisList)
                .build();
    }

    /**
     * 사용자가 속한 그룹의 오늘 지출 총액 조회
     */
    @Transactional("readTransactionManager")
    public GroupTodayExpenseResponse getTodayExpense(Long userId) {
        // 1. 사용자의 활성 그룹 조회
        GroupMember activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 그룹이 없습니다."));

        Long groupId = activeMember.getGroup().getGroupId();
        String groupName = activeMember.getGroup().getName();

        // 2. 오늘 날짜로 그룹 일별 요약 조회
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // ★ 파티션 확인/생성
        ensureAnalysisPartitionsExist(yearMonth);

        List<GroupDailySummary> todaySummaryList = groupDailySummaryRepository
                .findByGroupIdAndSummaryDate(groupId, today);

        // 3. 오늘 지출 데이터 반환
        if (!todaySummaryList.isEmpty()) {
            GroupDailySummary summary = todaySummaryList.get(0);
            return GroupTodayExpenseResponse.of(
                    groupId,
                    groupName,
                    summary.getTotalAmount(),
                    summary.getTransactionCount()
            );
        } else {
            // 오늘 거래내역이 없는 경우
            return GroupTodayExpenseResponse.of(groupId, groupName, 0L, 0);
        }
    }

    /**
     * 사용자가 속한 그룹의 마지막 업데이트 시각 조회
     */
    @Transactional(readOnly = true)
    public GroupLastUpdatedResponse getLastUpdated(Long userId) {
        // 1. 사용자의 활성 그룹 조회
        GroupMember activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 그룹이 없습니다."));

        Long groupId = activeMember.getGroup().getGroupId();

        // 2. 그룹 정보 조회 (updated_at 포함)
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));

        // 3. 마지막 업데이트 시각 반환
        return GroupLastUpdatedResponse.of(
                groupId,
                group.getName(),
                group.getUpdatedAt()
        );
    }

    // === Helper Methods ===

    /**
     * 그룹 멤버십 확인
     */
    public void validateGroupMembership(Long userId, Long groupId) {
        boolean isMember = groupMemberRepository.existsByGroup_GroupIdAndUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(groupId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
    }

    /**
     * 분석에 필요한 파티션 확인/생성 (단일 년월)
     */
    private void ensureAnalysisPartitionsExist(String yearMonth) {
        try {
            partitionManagerRepository.createMonthlyPartitionIfNotExists("user_daily_summary", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("user_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_daily_summary", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_member_monthly_expense_stats", yearMonth);
            partitionManagerRepository.createMonthlyPartitionIfNotExists("group_monthly_totals", yearMonth);

            log.debug("Ensured all analysis partitions exist for: {}", yearMonth);
        } catch (Exception e) {
            log.error("Failed to create analysis partitions for {}: {}", yearMonth, e.getMessage());
            throw AnalysisServerException.aggregationTableAccessFailed(e);
        }
    }

    /**
     * 분석에 필요한 파티션 확인/생성 (날짜 범위)
     */
    private void ensureAnalysisPartitionsExistForDateRange(LocalDate startDate, LocalDate endDate) {
        Set<String> requiredYearMonths = new HashSet<>();
        LocalDate current = startDate.withDayOfMonth(1);
        LocalDate endMonth = endDate.withDayOfMonth(1);

        while (!current.isAfter(endMonth)) {
            requiredYearMonths.add(current.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            current = current.plusMonths(1);
        }

        for (String yearMonth : requiredYearMonths) {
            ensureAnalysisPartitionsExist(yearMonth);
        }
    }

    /**
     * JSON에서 transaction IDs 파싱
     */
    private List<Long> parseTransactionIds(String transactionIdsJson) {
        try {
            if (transactionIdsJson == null || transactionIdsJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(transactionIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            // ★ JSON 파싱 실패는 데이터 무결성 문제
            log.error("JSON parsing failed for transaction IDs: {}", transactionIdsJson, e);
            throw new IllegalArgumentException("잘못된 거래 데이터 형식입니다", e);
        }
    }

    /**
     * 빈 일별 거래내역 응답 생성
     */
    private GroupDailyTransactionsResponse createEmptyDailyTransactionsResponse(Long groupId, String dateStr) {
        return GroupDailyTransactionsResponse.builder()
                .date(dateStr)
                .groupId(groupId)
                .totalAmount(0L)
                .totalCount(0)
                .transactions(List.of())
                .build();
    }

    /**
     * Transaction을 GroupTransactionDetail로 변환 (실명 포함)
     */
    private GroupDailyTransactionsResponse.GroupTransactionDetail convertToGroupTransactionDetail(
            Transaction transaction, Map<Long, MemberInfo> memberInfoMap) {

        String categoryName = "미분류";
        String description = "";

        // 거래 유형에 따라 카테고리명과 설명 조회
        if ("CARD".equals(transaction.getTransactionType())) {
            CardTransaction cardTransaction = cardTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (cardTransaction != null) {
                categoryName = getCategoryName(cardTransaction.getCategoryId());
                description = cardTransaction.getMerchantName();
            }
        } else if ("ACCOUNT".equals(transaction.getTransactionType())) {
            AccountTransaction accountTransaction = accountTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (accountTransaction != null) {
                categoryName = getCategoryName(accountTransaction.getCategoryId());
                description = accountTransaction.getAccountTransactionType();
            }
        }

        MemberInfo memberInfo = memberInfoMap.getOrDefault(transaction.getUserId(),
                new MemberInfo("알 수 없음", "알 수 없음"));

        return GroupDailyTransactionsResponse.GroupTransactionDetail.builder()
                .transactionId(transaction.getTransactionId())
                .transactionTime(transaction.getTransactionTime().withOffsetSameInstant(ZoneOffset.of("+09:00")).toLocalTime().toString())
                .amount(transaction.getAmount().longValue())
                .categoryName(categoryName)
                .isExcluded(transaction.getIsExcluded())
                .description(description)
                .memberName(memberInfo.getDisplayName())  // 그룹 닉네임
                .realName(memberInfo.getRealName())       // 실명
                .build();
    }

    /**
     * 카테고리명 조회
     */
    private String getCategoryName(Short categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("미분류");
    }

    private String getPreviousMonth(String yearMonth) {
        LocalDate date = LocalDate.parse(yearMonth + "-01");
        return date.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 특정 월의 거래 건수 조회
     */
    private int getTransactionCountForMonth(Long groupId, String yearMonth) {
        List<GroupDailySummary> dailySummaries = groupDailySummaryRepository
                .findByGroupIdAndYearMonthOrderBySummaryDateAsc(groupId, yearMonth);

        return dailySummaries.stream()
                .mapToInt(GroupDailySummary::getTransactionCount)
                .sum();
    }

    // Helper methods
    private GroupCategoryTransactionsResponse createEmptyCategoryTransactionsResponse(String yearMonth, Short categoryId) {
        return GroupCategoryTransactionsResponse.builder()
                .yearMonth(yearMonth)
                .categoryId(categoryId)
                .categoryName("미분류")
                .totalAmount(0L)
                .totalCount(0)
                .transactions(List.of())
                .build();
    }

    /**
     * 멤버 정보 맵 생성 (닉네임 + 실명)
     */
    private Map<Long, MemberInfo> createMemberInfoMap(Long groupId) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);
        return groupMembers.stream()
                .collect(Collectors.toMap(
                        member -> member.getUser().getUserId(),
                        member -> new MemberInfo(member.getDisplayName(), cryptoService.decryptAES(member.getUser().getUsername()))
                ));
    }

    /**
     * Transaction을 TransactionDetail로 변환 (실명 포함)
     */
    private GroupCategoryTransactionsResponse.TransactionDetail convertToTransactionDetail(
            Transaction transaction, MemberInfo memberInfo) {

        String merchantName = getMerchantNameForReport(transaction);

        return GroupCategoryTransactionsResponse.TransactionDetail.builder()
                .transactionId(transaction.getTransactionId())
                .transactionDate(transaction.getTransactionDate().toString())
                .transactionTime(transaction.getTransactionTime().withOffsetSameInstant(ZoneOffset.of("+09:00")).toLocalTime().toString())
                .amount(transaction.getAmount().longValue())
                .merchantName(merchantName)
                .memberName(memberInfo.getDisplayName())  // 그룹 닉네임
                .realName(memberInfo.getRealName())       // 실명
                .isExcluded(transaction.getIsExcluded())
                .build();
    }

    private String getMerchantNameForReport(Transaction transaction) {
        if ("CARD".equals(transaction.getTransactionType())) {
            return cardTransactionRepository.findByTransactionId(transaction.getTransactionId())
                    .map(CardTransaction::getMerchantName)
                    .orElse("");
        } else if ("ACCOUNT".equals(transaction.getTransactionType())) {
            return accountTransactionRepository.findByTransactionId(transaction.getTransactionId())
                    .map(AccountTransaction::getAccountTransactionType)
                    .orElse("");
        }
        return "";
    }
}