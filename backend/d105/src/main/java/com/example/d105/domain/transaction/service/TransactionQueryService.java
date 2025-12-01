package com.example.d105.domain.transaction.service;

import com.example.d105.common.exception.ResourceNotFoundException;
import com.example.d105.domain.transaction.dto.response.*;
import com.example.d105.domain.transaction.entity.AccountTransaction;
import com.example.d105.domain.transaction.entity.CardTransaction;
import com.example.d105.domain.transaction.entity.Category;
import com.example.d105.domain.transaction.entity.Transaction;
import com.example.d105.domain.transaction.event.CategoryChangedEvent;
import com.example.d105.domain.transaction.event.TransactionExcludedEvent;
import com.example.d105.domain.transaction.event.TransactionIncludedEvent;
import com.example.d105.domain.transaction.exception.TransactionServerException;
import com.example.d105.domain.transaction.repository.AccountTransactionRepository;
import com.example.d105.domain.transaction.repository.CardTransactionRepository;
import com.example.d105.domain.transaction.repository.CategoryRepository;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import com.example.d105.read.entity.UserDailySummary;
import com.example.d105.read.entity.UserMonthlyExpenseStats;
import com.example.d105.read.repository.UserDailySummaryRepository;
import com.example.d105.read.repository.UserMonthlyExpenseStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDailySummaryRepository userDailySummaryRepository;
    private final UserMonthlyExpenseStatsRepository userMonthlyExpenseStatsRepository;

    /**
     * 카테고리 목록 조회
     */
    public CategoryListResponse getCategoryList() {
        List<Category> categories = categoryRepository.findAll();

        List<CategoryListResponse.CategoryInfo> categoryInfos = categories.stream()
                .map(category -> CategoryListResponse.CategoryInfo.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .build())
                .collect(Collectors.toList());

        return CategoryListResponse.builder()
                .categories(categoryInfos)
                .build();
    }

    /**
     * 개인 거래내역 목록 조회 (월별 페이징)
     */
    public TransactionListResponse getTransactionList(Long userId, String yearMonth, int page, int size) {
        // yearMonth를 LocalDate 범위로 변환
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository
                .findByUserIdAndDateRangeAndNotExcluded(userId, startDate, endDate, pageable);

        List<TransactionListResponse.TransactionInfo> transactionInfos = transactionPage.getContent().stream()
                .map(this::convertToTransactionInfo)
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .transactions(transactionInfos)
                .totalElements((int) transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .message("거래내역 조회가 완료되었습니다.")
                .build();
    }

    /**
     * 개인 일별 거래내역 조회
     */
    public TransactionDailyResponse getDailyTransactions(Long userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateAndNotExcluded(userId, date);

        int totalAmount = transactions.stream()
                .mapToInt(Transaction::getAmount)
                .sum();

        List<TransactionDailyResponse.DailyTransactionInfo> dailyTransactionInfos = transactions.stream()
                .map(this::convertToDailyTransactionInfo)
                .collect(Collectors.toList());

        return TransactionDailyResponse.builder()
                .date(dateStr)
                .totalAmount(totalAmount)
                .transactionCount(transactions.size())
                .transactions(dailyTransactionInfos)
                .build();
    }

    /**
     * 거래내역 제외 처리
     */
    @Transactional
    public void excludeTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("거래내역", transactionId));

        // 본인 거래내역인지 확인
        if (!transaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 거래내역만 수정할 수 있습니다.");
        }

        if (transaction.getIsExcluded()) {
            throw new IllegalArgumentException("이미 제외된 거래내역입니다.");
        }

        transaction.setIsExcluded(true);
        transactionRepository.save(transaction);

        // 이벤트 발행으로 집계 테이블 업데이트
        String yearMonth = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        eventPublisher.publishEvent(new TransactionExcludedEvent(this, transaction, userId, yearMonth));

        log.info("Transaction excluded: transactionId={}, userId={}", transactionId, userId);
    }

    /**
     * 거래내역 포함 처리
     */
    @Transactional
    public void includeTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("거래내역", transactionId));

        // 본인 거래내역인지 확인
        if (!transaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 거래내역만 수정할 수 있습니다.");
        }

        if (!transaction.getIsExcluded()) {
            throw new IllegalArgumentException("이미 포함된 거래내역입니다.");
        }

        transaction.setIsExcluded(false);
        transactionRepository.save(transaction);

        // 이벤트 발행으로 집계 테이블 업데이트
        String yearMonth = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        eventPublisher.publishEvent(new TransactionIncludedEvent(this, transaction, userId, yearMonth));

        log.info("Transaction included: transactionId={}, userId={}", transactionId, userId);
    }

    /**
     * 거래내역 카테고리 변경 처리
     */
    @Transactional
    public void changeTransactionCategory(Long userId, Long transactionId, Short newCategoryId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("거래내역", transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 거래내역만 수정할 수 있습니다.");
        }

        if (!categoryRepository.existsById(newCategoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        // 변경 전에 기존 카테고리 ID 미리 저장
        Short oldCategoryId = getCurrentCategoryId(transaction);

        if (oldCategoryId.equals(newCategoryId)) {
            throw new IllegalArgumentException("동일한 카테고리입니다.");
        }

        // 카테고리 변경
        updateTransactionCategory(transaction, newCategoryId);

        // 이벤트 발행
        String yearMonth = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        eventPublisher.publishEvent(new CategoryChangedEvent(
                this, transaction, userId, yearMonth, oldCategoryId, newCategoryId));

        log.info("Transaction category changed: transactionId={}, userId={}, oldCategory={}, newCategory={}",
                transactionId, userId, oldCategoryId, newCategoryId);
    }

    /**
     * 현재 거래내역의 카테고리 ID 조회
     */
    private Short getCurrentCategoryId(Transaction transaction) {
        if ("ACCOUNT".equals(transaction.getTransactionType())) {
            return accountTransactionRepository.findByTransactionId(transaction.getTransactionId())
                    .map(AccountTransaction::getCategoryId)
                    .orElse((short) 1); // 미분류
        } else if ("CARD".equals(transaction.getTransactionType())) {
            return cardTransactionRepository.findByTransactionId(transaction.getTransactionId())
                    .map(CardTransaction::getCategoryId)
                    .orElse((short) 1); // 미분류
        }
        return (short) 1; // 기본값: 미분류
    }

    /**
     * 거래내역 카테고리 업데이트
     */
    private void updateTransactionCategory(Transaction transaction, Short newCategoryId) {
        if ("ACCOUNT".equals(transaction.getTransactionType())) {
            AccountTransaction accountTransaction = accountTransactionRepository
                    .findByTransactionId(transaction.getTransactionId())
                    .orElseThrow(() -> TransactionServerException.databaseError(
                            new RuntimeException("계좌 거래내역 데이터 정합성 오류")));

            accountTransaction.setCategoryId(newCategoryId);
            accountTransactionRepository.save(accountTransaction);

        } else if ("CARD".equals(transaction.getTransactionType())) {
            CardTransaction cardTransaction = cardTransactionRepository
                    .findByTransactionId(transaction.getTransactionId())
                    .orElseThrow(() -> TransactionServerException.databaseError(
                            new RuntimeException("카드 거래내역 데이터 정합성 오류")));

            cardTransaction.setCategoryId(newCategoryId);
            cardTransactionRepository.save(cardTransaction);
        }
    }

    /**
     * Transaction을 TransactionInfo로 변환
     */
    private TransactionListResponse.TransactionInfo convertToTransactionInfo(Transaction transaction) {
        String categoryName = "미분류";
        Short categoryId = (short) 99;
        String description = "";

        if ("ACCOUNT".equals(transaction.getTransactionType())) {
            AccountTransaction accountTransaction = accountTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (accountTransaction != null) {
                categoryId = accountTransaction.getCategoryId();
                categoryName = getCategoryName(categoryId);
                description = accountTransaction.getAccountTransactionType();
            }
        } else if ("CARD".equals(transaction.getTransactionType())) {
            CardTransaction cardTransaction = cardTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (cardTransaction != null) {
                categoryId = cardTransaction.getCategoryId();
                categoryName = getCategoryName(categoryId);
                description = cardTransaction.getMerchantName();
            }
        }

        return TransactionListResponse.TransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .transactionDate(transaction.getTransactionDate().toString())
                .transactionTime(transaction.getTransactionTime().withOffsetSameInstant(ZoneOffset.of("+09:00")).toLocalTime().toString())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .isExcluded(transaction.getIsExcluded())
                .description(description)
                .build();
    }

    /**
     * Transaction을 DailyTransactionInfo로 변환
     */
    private TransactionDailyResponse.DailyTransactionInfo convertToDailyTransactionInfo(Transaction transaction) {
        String categoryName = "미분류";
        String description = "";

        if ("ACCOUNT".equals(transaction.getTransactionType())) {
            AccountTransaction accountTransaction = accountTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (accountTransaction != null) {
                categoryName = getCategoryName(accountTransaction.getCategoryId());
                description = accountTransaction.getAccountTransactionType();
            }
        } else if ("CARD".equals(transaction.getTransactionType())) {
            CardTransaction cardTransaction = cardTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (cardTransaction != null) {
                categoryName = getCategoryName(cardTransaction.getCategoryId());
                description = cardTransaction.getMerchantName();
            }
        }

        return TransactionDailyResponse.DailyTransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .transactionTime(transaction.getTransactionTime().withOffsetSameInstant(ZoneOffset.of("+09:00")).toLocalTime().toString())
                .amount(transaction.getAmount())
                .categoryName(categoryName)
                .isExcluded(transaction.getIsExcluded())
                .description(description)
                .build();
    }

    /**
     * 카테고리명 조회 헬퍼 메소드
     */
    private String getCategoryName(Short categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("미분류");
    }

    /**
     * 최근 14일간 지출 총액 변화 조회
     */
    public TransactionRecentTrendResponse getRecentTrend(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(13); // 14일간 (오늘 포함)

        // UserDailySummary에서 최근 14일 데이터 조회
        List<UserDailySummary> summaries = userDailySummaryRepository
                .findRecentDailySummaries(userId, startDate);

        // 날짜별 맵 생성 (빠른 조회를 위해)
        Map<LocalDate, Long> summaryMap = summaries.stream()
                .collect(Collectors.toMap(
                        UserDailySummary::getSummaryDate,
                        UserDailySummary::getTotalAmount
                ));

        // 14일간 전체 날짜 생성 (데이터가 없는 날짜는 0으로 처리)
        List<TransactionRecentTrendResponse.DailyAmount> dailyAmounts = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            Long amount = summaryMap.getOrDefault(current, 0L);

            dailyAmounts.add(TransactionRecentTrendResponse.DailyAmount.builder()
                    .date(current.toString()) // "2025-09-01"
                    .totalAmount(amount)
                    .build());

            current = current.plusDays(1);
        }

        return TransactionRecentTrendResponse.builder()
                .dailyAmounts(dailyAmounts)
                .build();
    }

    /**
     * 개인 월간 일별 지출 총액 조회 (캘린더용)
     */
    public TransactionMonthlyCalendarResponse getMonthlyCalendar(Long userId, String yearMonth) {
        // UserDailySummary에서 해당 월의 모든 일별 데이터 조회
        List<UserDailySummary> summaries = userDailySummaryRepository
                .findByUserIdAndYearMonthOrderBySummaryDateAsc(userId, yearMonth);

        // 날짜별 맵 생성
        Map<LocalDate, UserDailySummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(
                        UserDailySummary::getSummaryDate,
                        summary -> summary
                ));

        // 해당 월의 모든 날짜 생성 (1일~말일)
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<TransactionMonthlyCalendarResponse.CalendarDay> monthlyCalendar = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            UserDailySummary summary = summaryMap.get(current);

            // 데이터가 없는 날은 0으로 설정 (정상적인 빈 데이터)
            Long totalAmount = (summary != null) ? summary.getTotalAmount() : 0L;
            Integer transactionCount = (summary != null) ? summary.getTransactionCount() : 0;

            monthlyCalendar.add(TransactionMonthlyCalendarResponse.CalendarDay.builder()
                    .date(current.toString()) // "2025-09-01"
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .build());

            current = current.plusDays(1);
        }

        return TransactionMonthlyCalendarResponse.builder()
                .monthlyCalendar(monthlyCalendar)
                .build();
    }

    /**
     * 개인 월간 카테고리별 파이차트 데이터 조회
     */
    public CategoryChartResponse getCategoryChart(Long userId, String yearMonth) {
        log.debug("Getting category chart for userId={}, yearMonth={}", userId, yearMonth);

        try {
            // 역정규화 테이블에서 카테고리별 통계 조회
            List<UserMonthlyExpenseStats> expenseStats = userMonthlyExpenseStatsRepository
                    .findByUserIdAndYearMonth(userId, yearMonth);

            // 데이터가 없는 경우 빈 결과 반환 (정상 응답)
            if (expenseStats.isEmpty()) {
                return CategoryChartResponse.builder()
                        .message("해당 월의 거래내역이 없습니다")
                        .data(CategoryChartResponse.CategoryChartData.builder()
                                .yearMonth(yearMonth)
                                .totalAmount(0L)
                                .totalTransactionCount(0)
                                .categoryStats(new ArrayList<>())
                                .build())
                        .build();
            }

            // 총 금액 및 거래 건수 계산
            Long totalAmount = expenseStats.stream()
                    .mapToLong(UserMonthlyExpenseStats::getExpenseAmount)
                    .sum();

            Integer totalTransactionCount = expenseStats.stream()
                    .mapToInt(UserMonthlyExpenseStats::getTransactionCount)
                    .sum();

            // 카테고리별 통계 변환
            List<CategoryChartResponse.CategoryStats> categoryStats = expenseStats.stream()
                    .filter(stat -> stat.getExpenseAmount() > 0) // 금액이 0인 카테고리 제외
                    .map(stat -> CategoryChartResponse.CategoryStats.builder()
                            .categoryId(stat.getCategoryId())
                            .categoryName(stat.getCategoryName())
                            .amount(stat.getExpenseAmount())
                            .transactionCount(stat.getTransactionCount())
                            .percentage(stat.getExpensePercentage().doubleValue())
                            .build())
                    .collect(Collectors.toList());

            CategoryChartResponse.CategoryChartData data = CategoryChartResponse.CategoryChartData.builder()
                    .yearMonth(yearMonth)
                    .totalAmount(totalAmount)
                    .totalTransactionCount(totalTransactionCount)
                    .categoryStats(categoryStats)
                    .build();

            return CategoryChartResponse.builder()
                    .message("카테고리별 차트 데이터 조회 성공")
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get category chart for userId={}, yearMonth={}: {}",
                    userId, yearMonth, e.getMessage());
            throw TransactionServerException.databaseError(e);
        }
    }

    /**
     * 개인 카테고리별 거래내역 상세 조회 (역정규화 테이블 최대 활용)
     */
    public CategoryChartListResponse getCategoryChartList(Long userId, String yearMonth, Short categoryId) {
        log.debug("Getting category chart list for userId={}, yearMonth={}, categoryId={}",
                userId, yearMonth, categoryId);

        try {
            // 해당 카테고리의 통계 정보 조회
            List<UserMonthlyExpenseStats> categoryStats = userMonthlyExpenseStatsRepository
                    .findByUserIdAndYearMonthAndCategoryId(userId, yearMonth, categoryId);

            // 데이터가 없는 경우 빈 결과 반환 (정상 응답)
            if (categoryStats.isEmpty()) {
                return CategoryChartListResponse.builder()
                        .message("해당 조건의 거래내역이 없습니다")
                        .data(CategoryChartListResponse.CategoryChartListData.builder()
                                .yearMonth(yearMonth)
                                .categoryId(categoryId)
                                .categoryName("미분류")
                                .totalAmount(0L)
                                .totalCount(0)
                                .transactions(new ArrayList<>())
                                .build())
                        .build();
            }

            UserMonthlyExpenseStats stat = categoryStats.get(0);

            // transaction_ids JSON에서 바로 거래내역 생성 (DB 조회 최소화)
            List<CategoryChartListResponse.TransactionDetail> transactionDetails =
                    createTransactionDetailsFromJson(stat.getTransactionIds());

            CategoryChartListResponse.CategoryChartListData data = CategoryChartListResponse.CategoryChartListData.builder()
                    .yearMonth(yearMonth)
                    .categoryId(categoryId)
                    .categoryName(stat.getCategoryName())
                    .totalAmount(stat.getExpenseAmount())
                    .totalCount(stat.getTransactionCount())
                    .transactions(transactionDetails)
                    .build();

            return CategoryChartListResponse.builder()
                    .message("카테고리별 거래내역 조회 성공")
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get category chart list for userId={}, yearMonth={}, categoryId={}: {}",
                    userId, yearMonth, categoryId, e.getMessage());
            throw TransactionServerException.databaseError(e);
        }
    }

    /**
     * JSON에서 거래내역 ID만으로 간단한 거래정보 생성
     * 필요시에만 실제 DB에서 상세정보 조회하는 방식으로 최적화
     */
    private List<CategoryChartListResponse.TransactionDetail> createTransactionDetailsFromJson(String transactionIdsJson) {
        List<Long> transactionIds = parseTransactionIds(transactionIdsJson);

        if (transactionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 간단한 조회만 수행 - transactions 테이블에서 기본 정보만
        return transactionRepository.findBasicTransactionInfoByIds(transactionIds)
                .stream()
                .map(this::convertToTransactionDetail)
                .collect(Collectors.toList());
    }

    /**
     * Transaction을 TransactionDetail로 변환 (상세 정보 포함)
     */
    private CategoryChartListResponse.TransactionDetail convertToTransactionDetail(Transaction transaction) {
        String merchantName = ""; // 기본값

        // 거래 유형에 따라 상점명 조회
        if ("CARD".equals(transaction.getTransactionType())) {
            CardTransaction cardTransaction = cardTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (cardTransaction != null) {
                merchantName = cardTransaction.getMerchantName();
            }
        } else if ("ACCOUNT".equals(transaction.getTransactionType())) {
            AccountTransaction accountTransaction = accountTransactionRepository
                    .findByTransactionId(transaction.getTransactionId()).orElse(null);
            if (accountTransaction != null) {
                merchantName = accountTransaction.getAccountTransactionType();
            }
        }

        return CategoryChartListResponse.TransactionDetail.builder()
                .transactionId(transaction.getTransactionId())
                .transactionDate(transaction.getTransactionDate().toString())
                .transactionTime(transaction.getTransactionTime().withOffsetSameInstant(ZoneOffset.of("+09:00")).toLocalTime().toString())
                .amount(transaction.getAmount().longValue())
                .merchantName(merchantName)
                .isExcluded(transaction.getIsExcluded())
                .build();
    }

    /**
     * JSON에서 transaction IDs 파싱
     */
    private List<Long> parseTransactionIds(String transactionIdsJson) {
        try {
            if (transactionIdsJson == null || transactionIdsJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(transactionIdsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            log.warn("Failed to parse transaction IDs JSON: {}", e.getMessage());
            throw TransactionServerException.databaseError(e);
        }
    }
}