package com.example.d105.domain.transaction.service;

import com.example.d105.domain.account.entity.UserAccount;
import com.example.d105.domain.account.entity.UserCard;
import com.example.d105.domain.account.repository.UserAccountRepository;
import com.example.d105.domain.account.repository.UserCardRepository;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.tracking.entity.GroupTrackingCard;
import com.example.d105.domain.tracking.entity.MemberTrackingAccount;
import com.example.d105.domain.tracking.repository.GroupTrackingCardRepository;
import com.example.d105.domain.tracking.repository.MemberTrackingAccountRepository;
import com.example.d105.domain.transaction.entity.AccountTransaction;
import com.example.d105.domain.transaction.entity.CardTransaction;
import com.example.d105.domain.transaction.entity.Transaction;
import com.example.d105.domain.transaction.event.BatchTransactionSavedEvent;
import com.example.d105.domain.transaction.repository.AccountTransactionRepository;
import com.example.d105.domain.transaction.repository.CardTransactionRepository;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.transaction.dto.response.SsafyAccountTransactionResponse;
import com.example.d105.ssafy.transaction.dto.response.SsafyCardTransactionResponse;
import com.example.d105.ssafy.transaction.service.SsafyTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.user.exception.UserException;
import com.example.d105.domain.transaction.exception.TransactionServerException;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final UserAccountRepository userAccountRepository;
    private final UserCardRepository userCardRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TransactionRepository transactionRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final SsafyTransactionService ssafyTransactionService;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberTrackingAccountRepository memberTrackingAccountRepository;
    private final GroupTrackingCardRepository groupTrackingCardRepository;
    private final GroupRepository groupRepository;

    private final UserService userService;
    private final CryptoService cryptoService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");



    @Transactional
    public Map<String, Object> syncAllTransactions(Long requestUserId) {
        log.info("Starting group transaction sync requested by userId: {}", requestUserId);

        try {
            // 1. 요청 사용자의 활성 그룹 조회
            GroupMember requesterMember = groupMemberRepository.findActiveGroupMemberByUserId(requestUserId)
                    .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND", "활성화된 그룹이 없습니다."));


            Long groupId = requesterMember.getGroup().getGroupId();
            log.info("Found active group {} for userId: {}", groupId, requestUserId);

            // 2. 해당 그룹의 승인되고 활성화된 멤버들 조회
            List<GroupMember> approvedMembers = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);

            // 나간 멤버 제외 (exitedAt이 null인 멤버만)
            List<GroupMember> activeApprovedMembers = approvedMembers.stream()
                    .filter(member -> member.getExitedAt() == null)
                    .collect(Collectors.toList());

            if (activeApprovedMembers.isEmpty()) {
                return createSyncResult("그룹에 승인된 활성 멤버가 없습니다.", 0);
            }

            log.info("Found {} approved active members in group {}", activeApprovedMembers.size(), groupId);

            // 3. 각 멤버별로 개인 거래내역 동기화 실행
            int totalNewTransactions = 0;
            int successCount = 0;
            int failCount = 0;
            List<String> failedUsers = new ArrayList<>();

            for (GroupMember member : activeApprovedMembers) {
                try {
                    Long memberId = member.getUser().getUserId();
                    log.info("Syncing transactions for group member userId: {}", memberId);

                    // 개인 동기화 로직 실행 (기존 로직을 별도 메서드로 분리해서 호출)
                    Map<String, Object> result = syncMyTransactions(memberId);
                    int newCount = (Integer) result.get("newTransactionCount");
                    totalNewTransactions += newCount;
                    successCount++;

                    log.info("Successfully synced {} new transactions for userId: {}", newCount, memberId);

                    // API 부하 방지를 위한 짧은 대기
                    Thread.sleep(200);

                } catch (Exception e) {
                    log.error("Failed to sync transactions for userId {}: {}", member.getUser().getUserId(), e.getMessage());
                    failedUsers.add("User " + member.getUser().getUserId());
                    failCount++;
                }
            }

            // 4. 결과 메시지 생성
            String message;
            if (failCount == 0) {
                message = String.format("그룹 전체 거래내역 동기화가 완료되었습니다. (총 %d명, 신규 %d건)",
                        successCount, totalNewTransactions);
            } else {
                message = String.format("그룹 거래내역 동기화가 완료되었습니다. 성공: %d명, 실패: %d명, 신규: %d건",
                        successCount, failCount, totalNewTransactions);
                if (!failedUsers.isEmpty()) {
                    message += " (실패: " + String.join(", ", failedUsers) + ")";
                }
            }

            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND", "그룹 정보를 찾을 수 없습니다."));
            group.setUpdatedAt(ZonedDateTime.now());
            groupRepository.save(group);

            log.info("Group sync completed: success={}, failed={}, totalNew={}", successCount, failCount, totalNewTransactions);
            return createSyncResult(message, totalNewTransactions);

        } catch (Exception e) {
            log.error("Group transaction sync failed for requestUserId {}: {}", requestUserId, e.getMessage(), e);
            throw TransactionServerException.syncFailed(e);

        }
    }

    /**
     * 4.1 API - 사용자 요청 기반 거래 내역 증분 업데이트
     */
    @Transactional
    public Map<String, Object> syncMyTransactions(Long userId) {
        log.info("Starting transaction sync for userId: {}", userId);

        try {
            // 1. 사용자 정보 및 userKey 조회
            String userKey;
            try {
                userKey = userService.getUserKey(userId);
            } catch (Exception e) {
                log.error("Failed to get userKey for userId {}: {}", userId, e.getMessage(), e);
                return createSyncResult("사용자 인증 정보를 찾을 수 없습니다.", 0);
            }

            // 2. 사용자의 활성 그룹 멤버십 조회 (한 개만 존재)
            GroupMember activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId)
                    .orElse(null);

            if (activeMember == null) {
                log.info("User {} has no active group membership", userId);
                return createSyncResult("활성화된 그룹이 없습니다.", 0);
            }

            // 3. 연결된 계좌/카드 조회
            List<UserAccount> connectedAccounts = getConnectedAccounts(activeMember.getMemberId());
            List<UserCard> connectedCards = getConnectedCards(activeMember.getMemberId());

            log.info("Found {} accounts and {} cards for user {}",
                    connectedAccounts.size(), connectedCards.size(), userId);

            // 4. 거래내역 동기화 수행
            int totalNewTransactions = 0;
            List<Long> newTransactionIds = new ArrayList<>();
            Set<String> affectedYearMonths = new HashSet<>();

            // 계좌 거래내역 동기화
            for (UserAccount account : connectedAccounts) {
                SyncResult result = syncAccountTransactions(account, userKey, userId);
                totalNewTransactions += result.newCount;
                newTransactionIds.addAll(result.transactionIds);
                affectedYearMonths.addAll(result.yearMonths);
            }

            // 카드 거래내역 동기화
            for (UserCard card : connectedCards) {
                SyncResult result = syncCardTransactions(card, userKey, userId);
                totalNewTransactions += result.newCount;
                newTransactionIds.addAll(result.transactionIds);
                affectedYearMonths.addAll(result.yearMonths);
            }

//            // 5. ★ 수정: 연결된 계좌/카드가 있다면 항상 이벤트 발행 (기존 거래 재집계)
//            if (!connectedAccounts.isEmpty() || !connectedCards.isEmpty()) {
//                // 영향받은 각 년월별로 이벤트 발행
//                for (String yearMonth : affectedYearMonths) {
//                    eventPublisher.publishEvent(new BatchTransactionSavedEvent(
//                            this, newTransactionIds, userId, yearMonth));
//
//                    log.info("Published BatchTransactionSavedEvent for userId={}, yearMonth={}, newTransactions={}",
//                            userId, yearMonth, totalNewTransactions);
//                }
//
//                // 영향받은 년월이 없다면 현재 월로 발행 (첫 동기화의 경우)
//                if (affectedYearMonths.isEmpty()) {
//                    String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//                    eventPublisher.publishEvent(new BatchTransactionSavedEvent(
//                            this, newTransactionIds, userId, currentYearMonth));
//
//                    log.info("Published current month BatchTransactionSavedEvent for userId={}, newTransactions={}",
//                            userId, totalNewTransactions);
//                }
//            }
            // 5. ☆ 개발용 수정: sync 버튼 누르면 항상 재계산 (신규 거래 여부 관계없이)
            if (!connectedAccounts.isEmpty() || !connectedCards.isEmpty()) {
                // 신규 거래가 있든 없든 항상 현재 월 재계산
                String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                eventPublisher.publishEvent(new BatchTransactionSavedEvent(
                        this, newTransactionIds, userId, currentYearMonth));

                // 영향받은 다른 월들도 재계산
                for (String yearMonth : affectedYearMonths) {
                    if (!yearMonth.equals(currentYearMonth)) {
                        eventPublisher.publishEvent(new BatchTransactionSavedEvent(
                                this, newTransactionIds, userId, yearMonth));
                    }
                }

                log.info("Force recalculation triggered for userId={}, currentMonth={}",
                        userId, currentYearMonth);
            }

            String message = String.format("거래내역 업데이트가 완료되었습니다. (신규 %d건)", totalNewTransactions);
            return createSyncResult(message, totalNewTransactions);

        } catch (Exception e) {
            log.error("Transaction sync failed for userId {}: {}", userId, e.getMessage(), e);
            return createSyncResult("거래내역 동기화 중 오류가 발생했습니다: " + e.getMessage(), 0);
        }
    }

    /**
     * 연결된 계좌 목록 조회
     */
    private List<UserAccount> getConnectedAccounts(Long memberId) {
        List<MemberTrackingAccount> trackingAccounts = memberTrackingAccountRepository
                .findConnectedAccountsByMemberId(memberId);

        return trackingAccounts.stream()
                .map(tracking -> userAccountRepository.findById(tracking.getAccountId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(account -> account.getIsActive())
                .collect(Collectors.toList());
    }

    /**
     * 연결된 카드 목록 조회
     */
    private List<UserCard> getConnectedCards(Long memberId) {
        List<GroupTrackingCard> trackingCards = groupTrackingCardRepository
                .findByMemberId(memberId);

        return trackingCards.stream()
                .filter(tracking -> tracking.getIsConnected())
                .map(tracking -> userCardRepository.findById(tracking.getCardId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(card -> card.getIsActive())
                .collect(Collectors.toList());
    }

    /**
     * ★ 수정: 계좌 거래내역 동기화 (SyncResult 반환, 항상 sync 시간 업데이트)
     */
    private SyncResult syncAccountTransactions(UserAccount account, String userKey, Long userId) {
        log.info("Syncing account transactions: accountId={}", account.getAccountId());

        ZonedDateTime lastSync = account.getLastTransactionSync();
        LocalDate startDate = (lastSync != null) ? lastSync.toLocalDate() : LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        String startDateStr = startDate.format(DATE_FORMATTER);
        String endDateStr = endDate.format(DATE_FORMATTER);

        // SSAFY API 호출
        SsafyAccountTransactionResponse response = ssafyTransactionService
                .getAccountTransactionHistory(userKey, account.getAccountNo(), startDateStr, endDateStr);

        List<Long> newTransactionIds = new ArrayList<>();
        Set<String> yearMonths = new HashSet<>();
        int newTransactionCount = 0;

        if (response.getRec() != null && response.getRec().getList() != null && !response.getRec().getList().isEmpty()) {
            for (SsafyAccountTransactionResponse.AccountTransactionRecord record : response.getRec().getList()) {
                try {
                    // 년월 수집 (이벤트 발행용)
                    LocalDate transactionDate = LocalDate.parse(record.getTransactionDate(), DATE_FORMATTER);
                    String yearMonth = transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    yearMonths.add(yearMonth);

                    // 중복 체크
                    if (transactionRepository.findByTransactionUniqueNo(record.getTransactionUniqueNo()).isPresent()) {
                        continue;
                    }

                    // Transaction 엔티티 생성
                    Transaction transaction = createTransactionFromAccountRecord(record, userId);
                    transaction = transactionRepository.save(transaction);
                    newTransactionIds.add(transaction.getTransactionId());

                    // AccountTransaction 엔티티 생성
                    AccountTransaction accountTransaction = createAccountTransaction(record, transaction.getTransactionId(), account.getAccountId());
                    accountTransactionRepository.save(accountTransaction);

                    newTransactionCount++;

                    log.debug("Saved account transaction: uniqueNo={}, amount={}",
                            record.getTransactionUniqueNo(), record.getTransactionBalance());

                } catch (Exception e) {
                    log.error("Failed to save account transaction: uniqueNo={}, error={}",
                            record.getTransactionUniqueNo(), e.getMessage());
                }
            }
        }

        // ★ 수정: API 호출이 성공했다면 항상 sync 시간 업데이트
        account.setLastTransactionSync(ZonedDateTime.now());
        userAccountRepository.save(account);

        log.info("Account sync completed: accountId={}, newTransactions={}",
                account.getAccountId(), newTransactionCount);

        return new SyncResult(newTransactionCount, newTransactionIds, yearMonths);
    }

    /**
     * ★ 수정: 카드 거래내역 동기화 (SyncResult 반환, 항상 sync 시간 업데이트)
     */
    private SyncResult syncCardTransactions(UserCard card, String userKey, Long userId) {
        log.info("Syncing card transactions: cardId={}", card.getCardId());

        // CVC 복호화
        String decryptedCvc = null;
        if (card.getCvcEncrypted() != null) {
            decryptedCvc = cryptoService.decryptAES(card.getCvcEncrypted());
        }

        ZonedDateTime lastSync = card.getLastTransactionSync();
        LocalDate startDate = (lastSync != null) ? lastSync.toLocalDate() : LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        String startDateStr = startDate.format(DATE_FORMATTER);
        String endDateStr = endDate.format(DATE_FORMATTER);

        // SSAFY API 호출
        SsafyCardTransactionResponse response = ssafyTransactionService
                .getCardTransactionHistory(userKey, card.getCardNo(), decryptedCvc, startDateStr, endDateStr);

        List<Long> newTransactionIds = new ArrayList<>();
        Set<String> yearMonths = new HashSet<>();
        int newTransactionCount = 0;

        if (response.getRec() != null && response.getRec().getTransactionList() != null && !response.getRec().getTransactionList().isEmpty()) {
            for (SsafyCardTransactionResponse.CardTransactionRecord record : response.getRec().getTransactionList()) {
                try {
                    // 년월 수집 (이벤트 발행용)
                    LocalDate transactionDate = LocalDate.parse(record.getTransactionDate(), DATE_FORMATTER);
                    String yearMonth = transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    yearMonths.add(yearMonth);

                    // 중복 체크
                    if (transactionRepository.findByTransactionUniqueNo(record.getTransactionUniqueNo()).isPresent()) {
                        continue;
                    }

                    // Transaction 엔티티 생성
                    Transaction transaction = createTransactionFromCardRecord(record, userId);
                    transaction = transactionRepository.save(transaction);
                    newTransactionIds.add(transaction.getTransactionId());

                    // CardTransaction 엔티티 생성
                    CardTransaction cardTransaction = createCardTransaction(record, transaction.getTransactionId(), card.getCardId());
                    cardTransactionRepository.save(cardTransaction);

                    newTransactionCount++;

                    log.debug("Saved card transaction: uniqueNo={}, amount={}, merchant={}",
                            record.getTransactionUniqueNo(), record.getTransactionBalance(), record.getMerchantName());

                } catch (Exception e) {
                    log.error("Failed to save card transaction: uniqueNo={}, error={}",
                            record.getTransactionUniqueNo(), e.getMessage());
                }
            }
        }

        // ★ 수정: API 호출이 성공했다면 항상 sync 시간 업데이트
        card.setLastTransactionSync(ZonedDateTime.now());
        userCardRepository.save(card);

        log.info("Card sync completed: cardId={}, newTransactions={}",
                card.getCardId(), newTransactionCount);

        return new SyncResult(newTransactionCount, newTransactionIds, yearMonths);
    }

    /**
     * 실제 SSAFY API 응답에 맞게 수정된 Transaction 생성
     */
    private Transaction createTransactionFromAccountRecord(SsafyAccountTransactionResponse.AccountTransactionRecord record, Long userId) {
        // 날짜/시간 파싱 (SSAFY 형식: yyyyMMdd, HHmmss)
        LocalDate transactionDate = LocalDate.parse(record.getTransactionDate(), DATE_FORMATTER);

        // 시간 파싱 및 OffsetTime 생성 (한국 시간대 +09:00)
        OffsetTime transactionTime = OffsetTime.parse(
                record.getTransactionTime() + "+09:00",
                DateTimeFormatter.ofPattern("HHmmssXXX")
        );

        return Transaction.builder()
                .userId(userId)
                .transactionUniqueNo(record.getTransactionUniqueNo())
                .transactionDate(transactionDate)
                .transactionTime(transactionTime)
                .transactionType("ACCOUNT")
                .amount(Integer.parseInt(record.getTransactionBalance()))
                .isExcluded(false)
                .build();
    }

    /**
     * 실제 SSAFY API 응답에 맞게 수정된 카드 Transaction 생성
     */
    private Transaction createTransactionFromCardRecord(SsafyCardTransactionResponse.CardTransactionRecord record, Long userId) {
        // 날짜/시간 파싱 (SSAFY 형식: yyyyMMdd, HHmmss)
        LocalDate transactionDate = LocalDate.parse(record.getTransactionDate(), DATE_FORMATTER);

        // 시간 파싱 및 OffsetTime 생성 (한국 시간대 +09:00)
        OffsetTime transactionTime = OffsetTime.parse(
                record.getTransactionTime() + "+09:00",
                DateTimeFormatter.ofPattern("HHmmssXXX")
        );

        return Transaction.builder()
                .userId(userId)
                .transactionUniqueNo(record.getTransactionUniqueNo())
                .transactionDate(transactionDate)
                .transactionTime(transactionTime)
                .transactionType("CARD")
                .amount(Integer.parseInt(record.getTransactionBalance()))
                .isExcluded(false)
                .build();
    }

    /**
     * 수정된 AccountTransaction 생성 (실제 응답 필드에 맞게)
     */
    private AccountTransaction createAccountTransaction(SsafyAccountTransactionResponse.AccountTransactionRecord record,
                                                        Long transactionId, Long accountId) {
        return AccountTransaction.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .accountTransactionType(record.getTransactionTypeName())
                .balanceAfter(Integer.parseInt(record.getTransactionAfterBalance()))
                .categoryId((short) 1)
                .build();
    }

    /**
     * 수정된 CardTransaction 생성 (실제 응답 필드에 맞게)
     */
    private CardTransaction createCardTransaction(SsafyCardTransactionResponse.CardTransactionRecord record,
                                                  Long transactionId, Long cardId) {
        // SSAFY에서 제공하는 카테고리 ID를 Short로 매핑
        Short categoryId = mapSsafyCategoryToShort(record.getCategoryId(), record.getCategoryName());

        return CardTransaction.builder()
                .transactionId(transactionId)
                .cardId(cardId)
                .merchantName(record.getMerchantName())
                .categoryId(categoryId)
                .build();
    }

    /**
     * SSAFY 카테고리 ID를 시스템 카테고리 ID로 매핑
     */
    private Short mapSsafyCategoryToShort(String ssafyCategoryId, String categoryName) {
        if (categoryName != null) {
            switch (categoryName) {
                case "주유": return (short) 2;
                case "대형마트": return (short) 3;
                case "교통": return (short) 4;
                case "교육/육아": return (short) 5;
                case "통신": return (short) 6;
                case "해외": return (short) 7;
                case "생활": return (short) 8;
                default: return (short) 1;
            }
        }
        return (short) 1;
    }

    /**
     * 동기화 결과 생성
     */
    private Map<String, Object> createSyncResult(String message, int newTransactionCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("newTransactionCount", newTransactionCount);
        return result;
    }

    /**
     * ★ 새로운 결과 클래스 - 동기화 결과 정보
     */
    private static class SyncResult {
        final int newCount;
        final List<Long> transactionIds;
        final Set<String> yearMonths;

        SyncResult(int newCount, List<Long> transactionIds, Set<String> yearMonths) {
            this.newCount = newCount;
            this.transactionIds = transactionIds;
            this.yearMonths = yearMonths;
        }
    }
}