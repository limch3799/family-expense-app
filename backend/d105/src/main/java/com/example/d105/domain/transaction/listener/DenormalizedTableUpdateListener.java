package com.example.d105.domain.transaction.listener;

import com.example.d105.domain.transaction.event.BatchTransactionSavedEvent;
import com.example.d105.domain.transaction.event.CategoryChangedEvent;
import com.example.d105.domain.transaction.event.TransactionExcludedEvent;
import com.example.d105.domain.transaction.event.TransactionIncludedEvent;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import com.example.d105.domain.transaction.service.AggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.d105.domain.account.event.AccountConnectionChangedEvent;
import com.example.d105.domain.account.event.CardConnectionChangedEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 거래내역 저장 후 역정규화 테이블 업데이트를 담당하는 비동기 리스너
 * 개인 집계 + 그룹 집계 동시 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DenormalizedTableUpdateListener {

    private final AggregationService aggregationService;
    private final TransactionRepository transactionRepository;

    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBatchTransactionSaved(BatchTransactionSavedEvent event) {
        log.info("🎯 Received BatchTransactionSavedEvent for userId={}, yearMonth={}, txIds={}",
                event.getUserId(), event.getYearMonth(), event.getTransactionIds());

        try {
            long startTime = System.currentTimeMillis();

            // 거래 날짜 조회
            Set<LocalDate> transactionDates = transactionRepository
                    .findDistinctDatesByTransactionIds(event.getTransactionIds());

            if (transactionDates.isEmpty() && event.getTransactionIds().isEmpty()) {
                // 강제 재계산인 경우 현재 날짜 추가
                transactionDates = Set.of(LocalDate.now());
                log.info("Force recalculation: added current date for userId={}", event.getUserId());
            }

            // ===== 개인 집계 =====
            aggregationService.updateUserMonthlyExpenseStats(event.getUserId(), event.getYearMonth(), List.of());
            aggregationService.updateUserMonthlyTotals(event.getUserId(), event.getYearMonth());

            if (!transactionDates.isEmpty()) {
                aggregationService.updateUserDailySummary(event.getUserId(), transactionDates);
            }

            // ===== 그룹 집계 =====
            aggregationService.updateGroupAggregations(event.getUserId(), event.getYearMonth());

            if (!transactionDates.isEmpty()) {
                log.info("Updating user daily summary for userId={}, dates={}",
                        event.getUserId(), transactionDates);
                aggregationService.updateGroupDailySummary(event.getUserId(), transactionDates);
            }

            long endTime = System.currentTimeMillis();
            log.info("Optimized aggregation completed in {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.error("Failed optimized aggregation: {}", e.getMessage(), e);
        }
    }



    /**
     * 거래내역 제외 이벤트 처리 - 개인 + 그룹 집계 재계산
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionExcluded(TransactionExcludedEvent event) {
        try {
            log.info("Processing transaction exclusion for transactionId={}",
                    event.getTransaction().getTransactionId());

            // ===== 개인 집계 처리 =====
            aggregationService.recalculateStatsAfterExclusion(
                    event.getUserId(),
                    event.getTransaction().getTransactionId(),
                    event.getYearMonth());

            LocalDate transactionDate = event.getTransaction().getTransactionDate();
            Set<LocalDate> dates = Set.of(transactionDate);
            aggregationService.updateUserDailySummary(event.getUserId(), dates);

            // ===== 그룹 집계 처리 =====
            aggregationService.updateGroupAggregations(event.getUserId(), event.getYearMonth());
            aggregationService.updateGroupDailySummary(event.getUserId(), dates);

            log.info("Transaction exclusion processed successfully for transactionId={} (including groups)",
                    event.getTransaction().getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process transaction exclusion for transactionId={}: {}",
                    event.getTransaction().getTransactionId(), e.getMessage(), e);
        }
    }

    /**
     * 거래내역 포함 이벤트 처리 - 개인 + 그룹 집계 재계산
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionIncluded(TransactionIncludedEvent event) {
        try {
            log.info("Processing transaction inclusion for transactionId={}",
                    event.getTransaction().getTransactionId());

            // ===== 개인 집계 처리 =====
            aggregationService.recalculateStatsAfterInclusion(
                    event.getUserId(),
                    event.getTransaction().getTransactionId(),
                    event.getYearMonth());

            LocalDate transactionDate = event.getTransaction().getTransactionDate();
            Set<LocalDate> dates = Set.of(transactionDate);
            aggregationService.updateUserDailySummary(event.getUserId(), dates);

            // ===== 그룹 집계 처리 =====
            aggregationService.updateGroupAggregations(event.getUserId(), event.getYearMonth());
            aggregationService.updateGroupDailySummary(event.getUserId(), dates);

            log.info("Transaction inclusion processed successfully for transactionId={} (including groups)",
                    event.getTransaction().getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process transaction inclusion for transactionId={}: {}",
                    event.getTransaction().getTransactionId(), e.getMessage(), e);
        }
    }

    /**
     * 계좌 연결 변경 이벤트 처리 - 전체 개인 + 그룹 재계산
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAccountConnectionChanged(AccountConnectionChangedEvent event) {
        try {
            log.info("Processing account connection change: userId={}, accountId={}, action={}",
                    event.getUserId(), event.getAccountId(), event.getAction());

            long startTime = System.currentTimeMillis();

            // ===== 개인 집계 처리 =====
            aggregationService.recalculateAllMonthsForUser(event.getUserId());
            aggregationService.recalculateAllDailySummariesForUser(event.getUserId());

            // ===== 그룹 집계 처리 =====
            aggregationService.recalculateAllGroupsForUser(event.getUserId());

            long endTime = System.currentTimeMillis();
            log.info("Account connection change processed successfully in {}ms: userId={}, action={} (including groups)",
                    (endTime - startTime), event.getUserId(), event.getAction());

        } catch (Exception e) {
            log.error("Failed to process account connection change: userId={}, accountId={}, action={}, error={}",
                    event.getUserId(), event.getAccountId(), event.getAction(), e.getMessage(), e);
        }
    }

    /**
     * 카드 연결 변경 이벤트 처리 - 전체 개인 + 그룹 재계산
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCardConnectionChanged(CardConnectionChangedEvent event) {
        try {
            log.info("Processing card connection change: userId={}, cardId={}, action={}",
                    event.getUserId(), event.getCardId(), event.getAction());

            long startTime = System.currentTimeMillis();

            // ===== 개인 집계 처리 =====
            aggregationService.recalculateAllMonthsForUser(event.getUserId());
            aggregationService.recalculateAllDailySummariesForUser(event.getUserId());

            // ===== 그룹 집계 처리 =====
            aggregationService.recalculateAllGroupsForUser(event.getUserId());

            long endTime = System.currentTimeMillis();
            log.info("Card connection change processed successfully in {}ms: userId={}, action={} (including groups)",
                    (endTime - startTime), event.getUserId(), event.getAction());

        } catch (Exception e) {
            log.error("Failed to process card connection change: userId={}, cardId={}, action={}, error={}",
                    event.getUserId(), event.getCardId(), event.getAction(), e.getMessage(), e);
        }
    }

    /**
     * 카테고리 변경 이벤트 처리 - 개인 + 그룹 월별 집계 재계산
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCategoryChanged(CategoryChangedEvent event) {
        try {
            log.info("Processing category change for transactionId={}, {} -> {}",
                    event.getTransaction().getTransactionId(),
                    event.getOldCategoryId(), event.getNewCategoryId());

            // ===== 개인 집계 처리 =====
            aggregationService.recalculateStatsAfterCategoryChange(
                    event.getUserId(),
                    event.getTransaction().getTransactionId(),
                    event.getYearMonth(),
                    event.getOldCategoryId(),
                    event.getNewCategoryId());

            // ===== 그룹 집계 처리 =====
            aggregationService.updateGroupAggregations(event.getUserId(), event.getYearMonth());
            // 일별 요약은 카테고리와 무관하므로 업데이트하지 않음 (총액과 거래건수만 저장)

            log.info("Category change processed successfully for transactionId={} (including groups)",
                    event.getTransaction().getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process category change for transactionId={}: {}",
                    event.getTransaction().getTransactionId(), e.getMessage(), e);
        }
    }
}