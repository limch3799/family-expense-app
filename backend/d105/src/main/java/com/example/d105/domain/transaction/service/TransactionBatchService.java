package com.example.d105.domain.transaction.service;

import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.domain.user.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionBatchService {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final GroupMemberRepository groupMemberRepository;
    private final FcmService fcmService;

    /**
     * 모든 활성 사용자의 거래내역 동기화
     */
    public void syncAllActiveUsers() {
        List<Long> activeUserIds = userRepository.findActiveUserIds();

        log.info("Found {} active users for batch sync", activeUserIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long userId : activeUserIds) {
            try {
                log.debug("Syncing transactions for user: {}", userId);
                transactionService.syncAllTransactions(userId);
                fcmService.sendMessageFinal(groupMemberRepository.findActiveGroupIdByUserId(userId), "✨ 거래 내역 동기화 알림!" ,"자동으로 거래 내역을 동기화 했습니다",3);
                successCount++;

                // API 부하 방지를 위한 짧은 대기
                Thread.sleep(200);

            } catch (Exception e) {
                log.error("Failed to sync transactions for user {}: {}", userId, e.getMessage());
                failCount++;
            }
        }

        log.info("Batch sync completed. Success: {}, Failed: {}, Total: {}",
                successCount, failCount, activeUserIds.size());
    }
}