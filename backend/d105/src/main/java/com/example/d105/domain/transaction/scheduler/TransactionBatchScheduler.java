package com.example.d105.domain.transaction.scheduler;

import com.example.d105.domain.transaction.service.TransactionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionBatchScheduler {

    private final TransactionBatchService batchService;

    /**
     * 매일 오전 2시에 모든 활성 사용자의 거래내역 동기화
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncAllUsersDaily() {
        log.info("Starting daily transaction batch sync");
        try {
            batchService.syncAllActiveUsers();
            log.info("Daily transaction batch sync completed successfully");
        } catch (Exception e) {
            log.error("Daily transaction batch sync failed: {}", e.getMessage(), e);
        }
    }
}