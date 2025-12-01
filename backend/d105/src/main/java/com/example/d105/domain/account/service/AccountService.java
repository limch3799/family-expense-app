package com.example.d105.domain.account.service;

import com.example.d105.common.exception.ResourceNotFoundException;
import com.example.d105.domain.account.dto.request.AccountGroupConnectRequest;
import com.example.d105.domain.account.dto.request.AccountGroupDisconnectRequest;
import com.example.d105.domain.account.dto.response.*;
import com.example.d105.domain.account.entity.UserAccount;
import com.example.d105.domain.account.entity.UserCard;
import com.example.d105.domain.account.exception.AccountServerException;
import com.example.d105.domain.account.repository.UserAccountRepository;
import com.example.d105.domain.account.repository.UserCardRepository;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.tracking.entity.GroupTrackingCard;
import com.example.d105.domain.tracking.entity.MemberTrackingAccount;
import com.example.d105.domain.tracking.repository.GroupTrackingCardRepository;
import com.example.d105.domain.tracking.repository.MemberTrackingAccountRepository;
import com.example.d105.domain.transaction.service.AggregationService;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.account.dto.response.SsafyAccountListResponse;
import com.example.d105.ssafy.account.dto.response.SsafyCardListResponse;
import com.example.d105.ssafy.account.service.SsafyAccountService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.example.d105.domain.account.event.AccountConnectionChangedEvent;
import com.example.d105.domain.account.event.CardConnectionChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserCardRepository userCardRepository;
    private final SsafyAccountService ssafyAccountService;
    private final CryptoService cryptoService;

    private final GroupMemberRepository groupMemberRepository;
    private final MemberTrackingAccountRepository memberTrackingAccountRepository;
    private final GroupTrackingCardRepository groupTrackingCardRepository;
    private final AggregationService aggregationService;
    private final ApplicationEventPublisher eventPublisher;

    // 3.1 기존 계좌/카드 목록 조회
    @Transactional
    public AccountListResponse getAccountAndCardList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자"));

        String encryptedUserKey = user.getUserKey();
        String userKey = cryptoService.decryptAES(encryptedUserKey);
        List<AccountInfo> accounts = new ArrayList<>();
        List<CardInfo> cards = new ArrayList<>();
        StringBuilder messageBuilder = new StringBuilder();

        Set<Long> connectedAccountIds = getConnectedAccountIds(userId);
        Set<Long> connectedCardIds = getConnectedCardIds(userId);

        try {
            SsafyAccountListResponse accountResponse = ssafyAccountService.getAccountList(userKey);
            accounts = processAccountList(userId, accountResponse, connectedAccountIds);
            messageBuilder.append("계좌 목록을 성공적으로 불러왔습니다. ");
            log.info("Successfully processed {} accounts for user {}", accounts.size(), userId);
        } catch (Exception e) {
            log.error("Failed to process account list for user {}: {}", userId, e.getMessage());
            messageBuilder.append("계좌 목록 조회에 실패했습니다. ");
        }

        try {
            SsafyCardListResponse cardResponse = ssafyAccountService.getCardList(userKey);
            cards = processCardList(userId, cardResponse, connectedCardIds);
            messageBuilder.append("카드 목록을 성공적으로 불러왔습니다. ");
            log.info("Successfully processed {} cards for user {}", cards.size(), userId);
        } catch (Exception e) {
            log.error("Failed to process card list for user {}: {}", userId, e.getMessage());
            messageBuilder.append("카드 목록 조회에 실패했습니다. ");
        }

        String finalMessage = messageBuilder.toString().trim();
        if (finalMessage.isEmpty()) {
            finalMessage = "계좌 및 카드 목록 조회에 모두 실패했습니다.";
        }

        return AccountListResponse.builder()
                .accounts(accounts)
                .cards(cards)
                .message(finalMessage)
                .build();
    }

    // 3.2 계좌/카드 그룹 연결
    @Transactional
    public AccountGroupConnectResponse connectAccountsAndCards(Long userId, AccountGroupConnectRequest request) {
        GroupMember groupMember = groupMemberRepository.findActiveGroupMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("활성 그룹"));

        if (!groupMember.getGroup().getGroupId().equals(request.getGroupId())) {
            throw new AccessDeniedException("해당 그룹에 접근 권한이 없습니다.");
        }

        List<AccountConnectionResult> accountResults = new ArrayList<>();
        List<AccountConnectionResult> cardResults = new ArrayList<>();

        if (request.getAccountIds() != null && !request.getAccountIds().isEmpty()) {
            accountResults = processAccountConnections(userId, groupMember.getMemberId(), request.getAccountIds());
        }

        if (request.getCardIds() != null && !request.getCardIds().isEmpty()) {
            cardResults = processCardConnections(userId, groupMember.getMemberId(), request.getCardIds());
        }

        String finalMessage = generateConnectionMessage(accountResults, cardResults);

        return AccountGroupConnectResponse.builder()
                .connectedAccounts(accountResults)
                .connectedCards(cardResults)
                .message(finalMessage)
                .build();
    }

    // 3.3 계좌/카드 그룹 연결 해제
    @Transactional
    public AccountGroupDisconnectResponse disconnectAccountOrCard(Long userId, AccountGroupDisconnectRequest request) {
        if ((request.getAccountId() != null && request.getCardId() != null) ||
                (request.getAccountId() == null && request.getCardId() == null)) {
            throw new IllegalArgumentException("계좌 또는 카드 중 하나만 선택해야 합니다.");
        }

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndUserIdAndActive(request.getGroupId(), userId)
                .orElseThrow(() -> new AccessDeniedException("해당 그룹의 멤버가 아니거나 권한이 없습니다."));

        if (request.getAccountId() != null) {
            return disconnectAccount(userId, groupMember.getMemberId(), request.getAccountId());
        } else {
            return disconnectCard(userId, groupMember.getMemberId(), request.getCardId());
        }
    }

    // 3.4 내 그룹 연결 계좌/카드 목록 조회
    @Transactional(readOnly = true)
    public AccountGroupConnectedListResponse getMyConnectedAccountsAndCards(Long userId, Long groupId) {
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndUserIdAndActive(groupId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 그룹의 멤버가 아니거나 권한이 없습니다."));

        // 연결된 계좌 목록 조회 (단순화)
        List<MemberTrackingAccount> connectedAccounts =
                memberTrackingAccountRepository.findConnectedAccountsByGroupIdAndUserId(groupId, userId);

        List<AccountConnectedAccountInfo> accountInfos = connectedAccounts.stream()
                .map(mta -> {
                    UserAccount account = userAccountRepository.findById(mta.getAccountId()).orElse(null);
                    if (account == null || !account.getIsActive()) return null;
                    return AccountConnectedAccountInfo.builder()
                            .accountId(account.getAccountId())
                            .accountNo(account.getAccountNo())
                            .bankName(account.getBankName())
                            .build();
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());

        // 연결된 카드 목록 조회 (단순화)
        List<GroupTrackingCard> connectedCards =
                groupTrackingCardRepository.findConnectedCardsByGroupIdAndUserId(groupId, userId);

        List<AccountConnectedCardInfo> cardInfos = connectedCards.stream()
                .map(gtc -> {
                    UserCard card = userCardRepository.findById(gtc.getCardId()).orElse(null);
                    if (card == null || !card.getIsActive()) return null;
                    return AccountConnectedCardInfo.builder()
                            .cardId(card.getCardId())
                            .cardNo(card.getCardNo())
                            .cardCompany(card.getCardCompany())
                            .build();
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());

        String message = String.format("내 연결 계좌/카드 조회 완료 (계좌 %d개, 카드 %d개)",
                accountInfos.size(), cardInfos.size());

        return AccountGroupConnectedListResponse.builder()
                .myConnectedAccounts(accountInfos)
                .myConnectedCards(cardInfos)
                .message(message)
                .build();
    }

    // ========== Private Methods ==========

    private List<AccountInfo> processAccountList(Long userId, SsafyAccountListResponse response, Set<Long> connectedAccountIds) {
        List<AccountInfo> accountInfos = new ArrayList<>();
        if (response == null || response.getRecords() == null) {
            return accountInfos;
        }

        for (SsafyAccountListResponse.AccountRecord record : response.getRecords()) {
            if (!"1".equals(record.getAccountTypeCode())) {
                continue;
            }

            try {
                // 기존 계좌 조회 (있으면 기존 것 사용, 없으면 새로 생성)
                UserAccount userAccount = userAccountRepository
                        .findByUserIdAndAccountNoAndIsActiveTrue(userId, record.getAccountNo())
                        .orElseGet(() -> {
                            UserAccount newAccount = createUserAccountFromRecord(userId, record);
                            return userAccountRepository.save(newAccount);
                        });

                // 응답에는 항상 포함
                AccountInfo accountInfo = AccountInfo.builder()
                        .accountId(userAccount.getAccountId())
                        .accountNo(userAccount.getAccountNo())
                        .bankName(userAccount.getBankName())
                        .isConnectedToGroup(connectedAccountIds.contains(userAccount.getAccountId()))
                        .build();

                accountInfos.add(accountInfo);

            } catch (Exception e) {
                log.error("Failed to process account {}: {}", record.getAccountNo(), e.getMessage());
            }
        }
        return accountInfos;
    }

    private List<CardInfo> processCardList(Long userId, SsafyCardListResponse response, Set<Long> connectedCardIds) {
        List<CardInfo> cardInfos = new ArrayList<>();
        if (response == null || response.getRecords() == null) {
            return cardInfos;
        }

        for (SsafyCardListResponse.CardRecord record : response.getRecords()) {
            try {
                // 기존 카드 조회 (있으면 기존 것 사용, 없으면 새로 생성)
                UserCard userCard = userCardRepository
                        .findByUserIdAndCardNoAndIsActiveTrue(userId, record.getCardNo())
                        .orElseGet(() -> {
                            UserCard newCard = createUserCardFromRecord(userId, record);
                            return userCardRepository.save(newCard);
                        });

                // 응답에는 항상 포함
                CardInfo cardInfo = CardInfo.builder()
                        .cardId(userCard.getCardId())
                        .cardNo(userCard.getCardNo())
                        .cardCompany(userCard.getCardCompany())
                        .isConnectedToGroup(connectedCardIds.contains(userCard.getCardId()))
                        .build();

                cardInfos.add(cardInfo);

            } catch (Exception e) {
                log.error("Failed to process card {}: {}", record.getCardNo(), e.getMessage());
            }
        }
        return cardInfos;
    }

    private UserAccount createUserAccountFromRecord(Long userId, SsafyAccountListResponse.AccountRecord record) {
        ZonedDateTime expiryDateTime = parseExpiryDate(record.getAccountExpiryDate());
        return UserAccount.builder()
                .userId(userId)
                .accountNo(record.getAccountNo())
                .bankCode(record.getBankCode())
                .bankName(record.getBankName())
                .accountExpiryDate(expiryDateTime)
                .loadedAt(ZonedDateTime.now())
                .lastTransactionSync(null)
                .isActive(true)
                .build();
    }

    private UserCard createUserCardFromRecord(Long userId, SsafyCardListResponse.CardRecord record) {
        ZonedDateTime expiryDateTime = parseExpiryDate(record.getCardExpiryDate());

        // CVC 암호화 처리 (nullable)
        String encryptedCvc = null;
        if (record.getCvc() != null && !record.getCvc().trim().isEmpty()) {
            try {
                encryptedCvc = cryptoService.encryptAES(record.getCvc());
                log.debug("CVC encrypted successfully for card: {}", record.getCardNo());
            } catch (Exception e) {
                log.error("Failed to encrypt CVC for card {}: {}", record.getCardNo(), e.getMessage());
                throw AccountServerException.encryptionError(e);
            }
        } else {
            log.info("CVC not provided for card: {}", record.getCardNo());
            // CVC가 없어도 카드 등록은 진행
        }

        return UserCard.builder()
                .userId(userId)
                .cardNo(record.getCardNo())
                .cardCompany(record.getCardIssuerName())
                .cardExpiryDate(expiryDateTime)
                .withdrawalAccountNo(record.getWithdrawalAccountNo())
                .cvcEncrypted(encryptedCvc) // 암호화된 CVC 저장
                .loadedAt(ZonedDateTime.now())
                .lastTransactionSync(null)
                .isActive(true)
                .build();
    }

    private ZonedDateTime parseExpiryDate(String dateString) {
        if (dateString == null || dateString.length() != 8) {
            throw new IllegalArgumentException("만료일 형식이 올바르지 않습니다: " + dateString);
        }
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDateTime localDateTime = date.atTime(23, 59, 59);
            return localDateTime.atZone(ZoneId.systemDefault());
        } catch (Exception e) {
            throw AccountServerException.databaseError(e);
        }
    }

    private List<AccountConnectionResult> processAccountConnections(Long userId, Long memberId, List<Long> accountIds) {
        List<AccountConnectionResult> results = new ArrayList<>();
        boolean hasSuccessfulConnection = false; // 추가

        for (Long accountId : accountIds) {
            try {
                UserAccount account = userAccountRepository.findById(accountId)
                        .filter(acc -> acc.getUserId().equals(userId) && acc.getIsActive())
                        .orElseThrow(() -> AccountServerException.accountNotFound(accountId));

                Optional<MemberTrackingAccount> existingTracking =
                        memberTrackingAccountRepository.findByMemberIdAndAccountId(memberId, accountId);

                if (existingTracking.isPresent()) {
                    memberTrackingAccountRepository.updateConnectionStatus(memberId, accountId, true);
                    log.info("Updated existing account tracking for member: {}, account: {}", memberId, accountId);
                } else {
                    MemberTrackingAccount newTracking = MemberTrackingAccount.builder()
                            .memberId(memberId)
                            .accountId(accountId)
                            .isConnected(true)
                            .build();
                    memberTrackingAccountRepository.save(newTracking);
                    log.info("Created new account tracking for member: {}, account: {}", memberId, accountId);
                }

                results.add(AccountConnectionResult.accountSuccess(accountId));
                hasSuccessfulConnection = true; // 추가

            } catch (Exception e) {
                log.error("Failed to connect account {} for member {}: {}", accountId, memberId, e.getMessage());
                results.add(AccountConnectionResult.accountFailure(accountId, e.getMessage()));
            }
        }

        // 성공한 연결이 있으면 재계산 트리거 (추가)
        for (AccountConnectionResult result : results) {
            if ("success".equals(result.getStatus())) {
                publishAccountConnectionEvent(userId, result.getAccountId(), "계좌 연결");
            }
        }

        return results;
    }

    // processCardConnections 메서드 수정
    private List<AccountConnectionResult> processCardConnections(Long userId, Long memberId, List<Long> cardIds) {
        List<AccountConnectionResult> results = new ArrayList<>();
        boolean hasSuccessfulConnection = false; // 추가

        for (Long cardId : cardIds) {
            try {
                UserCard card = userCardRepository.findById(cardId)
                        .filter(c -> c.getUserId().equals(userId) && c.getIsActive())
                        .orElseThrow(() -> AccountServerException.cardNotFound(cardId));

                Optional<GroupTrackingCard> existingTracking =
                        groupTrackingCardRepository.findByMemberIdAndCardId(memberId, cardId);

                if (existingTracking.isPresent()) {
                    groupTrackingCardRepository.updateConnectionStatus(memberId, cardId, true);
                    log.info("Updated existing card tracking for member: {}, card: {}", memberId, cardId);
                } else {
                    GroupTrackingCard newTracking = GroupTrackingCard.builder()
                            .memberId(memberId)
                            .cardId(cardId)
                            .isConnected(true)
                            .build();
                    groupTrackingCardRepository.save(newTracking);
                    log.info("Created new card tracking for member: {}, card: {}", memberId, cardId);
                }

                results.add(AccountConnectionResult.cardSuccess(cardId));
                hasSuccessfulConnection = true; // 추가

            } catch (Exception e) {
                log.error("Failed to connect card {} for member {}: {}", cardId, memberId, e.getMessage());
                results.add(AccountConnectionResult.cardFailure(cardId, e.getMessage()));
            }
        }

        // 성공한 연결이 있으면 재계산 트리거 (추가)
        if (hasSuccessfulConnection) {
            // 성공한 각 카드에 대해 이벤트 발행
            for (AccountConnectionResult result : results) {
                if ("success".equals(result.getStatus())) {
                    publishCardConnectionEvent(userId, result.getCardId(), "카드 연결");
                }
            }
        }

        return results;
    }

    // disconnectAccount 메서드 수정
    private AccountGroupDisconnectResponse disconnectAccount(Long userId, Long memberId, Long accountId) {
        UserAccount account = userAccountRepository.findById(accountId)
                .filter(acc -> acc.getUserId().equals(userId) && acc.getIsActive())
                .orElseThrow(() ->  AccountServerException.accountNotFound(accountId));

        MemberTrackingAccount tracking = memberTrackingAccountRepository.findByMemberIdAndAccountId(memberId, accountId)
                .orElseThrow(() -> AccountServerException.notConnected(accountId.toString()));

        if (!tracking.getIsConnected()) {
            throw new IllegalArgumentException("이미 연결 해제된 계좌입니다.");
        }

        memberTrackingAccountRepository.updateConnectionStatus(memberId, accountId, false);
        log.info("Disconnected account {} for member {}", accountId, memberId);

        // 재계산 트리거 (추가)
        publishAccountConnectionEvent(userId, accountId, "계좌 해제");

        return AccountGroupDisconnectResponse.accountSuccess(accountId);
    }

    // disconnectCard 메서드 수정
    private AccountGroupDisconnectResponse disconnectCard(Long userId, Long memberId, Long cardId) {
        UserCard card = userCardRepository.findById(cardId)
                .filter(c -> c.getUserId().equals(userId) && c.getIsActive())
                .orElseThrow(() -> AccountServerException.cardNotFound(cardId));

        GroupTrackingCard tracking = groupTrackingCardRepository.findByMemberIdAndCardId(memberId, cardId)
                .orElseThrow(() -> AccountServerException.notConnected(cardId.toString()));

        if (!tracking.getIsConnected()) {
            throw new IllegalArgumentException("이미 연결 해제된 카드입니다.");
        }

        groupTrackingCardRepository.updateConnectionStatus(memberId, cardId, false);
        log.info("Disconnected card {} for member {}", cardId, memberId);

        // 재계산 트리거 (추가)
        publishCardConnectionEvent(userId, cardId, "카드 해제");

        return AccountGroupDisconnectResponse.cardSuccess(cardId);
    }

    private void publishAccountConnectionEvent(Long userId, Long accountId, String action) {
        eventPublisher.publishEvent(new AccountConnectionChangedEvent(this, userId, accountId, action));
        log.info("Published AccountConnectionChangedEvent: userId={}, accountId={}, action={}",
                userId, accountId, action);
    }

    private void publishCardConnectionEvent(Long userId, Long cardId, String action) {
        eventPublisher.publishEvent(new CardConnectionChangedEvent(this, userId, cardId, action));
        log.info("Published CardConnectionChangedEvent: userId={}, cardId={}, action={}",
                userId, cardId, action);
    }

    private String generateConnectionMessage(List<AccountConnectionResult> accountResults, List<AccountConnectionResult> cardResults) {
        int successfulAccounts = (int) accountResults.stream().filter(r -> "success".equals(r.getStatus())).count();
        int successfulCards = (int) cardResults.stream().filter(r -> "success".equals(r.getStatus())).count();
        int totalAccounts = accountResults.size();
        int totalCards = cardResults.size();

        StringBuilder messageBuilder = new StringBuilder();
        if (totalAccounts > 0) {
            messageBuilder.append(String.format("계좌 %d개 중 %d개 연결 완료", totalAccounts, successfulAccounts));
        }
        if (totalCards > 0) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append(", ");
            }
            messageBuilder.append(String.format("카드 %d개 중 %d개 연결 완료", totalCards, successfulCards));
        }
        if (messageBuilder.length() == 0) {
            return "연결할 계좌 또는 카드가 없습니다.";
        }
        return messageBuilder.toString() + ".";
    }

    private Set<Long> getConnectedAccountIds(Long userId) {
        Optional<GroupMember> activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId);
        if (activeMember.isEmpty()) {
            return Set.of();
        }

        return memberTrackingAccountRepository
                .findConnectedAccountsByMemberId(activeMember.get().getMemberId())
                .stream()
                .map(MemberTrackingAccount::getAccountId)
                .collect(Collectors.toSet());
    }

    private Set<Long> getConnectedCardIds(Long userId) {
        Optional<GroupMember> activeMember = groupMemberRepository.findActiveGroupMemberByUserId(userId);
        if (activeMember.isEmpty()) {
            return Set.of();
        }

        return groupTrackingCardRepository
                .findByMemberId(activeMember.get().getMemberId())
                .stream()
                .filter(GroupTrackingCard::getIsConnected)  // is_connected=true인 것만
                .map(GroupTrackingCard::getCardId)
                .collect(Collectors.toSet());
    }

    /**
     * 필요시 CVC를 복호화하여 반환 (거래 API 호출 시 사용)
     * @param userCard 사용자 카드 정보
     * @return 복호화된 CVC (CVC가 없으면 null 반환)
     */
    public String getDecryptedCvc(UserCard userCard) {
        if (userCard == null) {
            throw AccountServerException.cardNotFound();
        }

        if (userCard.getCvcEncrypted() == null || userCard.getCvcEncrypted().trim().isEmpty()) {
            log.info("CVC not available for card: {}", userCard.getCardNo());
            return null; // CVC가 없는 경우 null 반환
        }

        try {
            return cryptoService.decryptAES(userCard.getCvcEncrypted());
        } catch (Exception e) {
            log.error("Failed to decrypt CVC for card {}: {}", userCard.getCardNo(), e.getMessage());
            throw AccountServerException.decryptionError(e);
        }
    }

    /**
     * 카드 유효성 검증 (CVC 포함)
     * @param cardId 카드 ID
     * @param userId 사용자 ID
     * @return 검증된 카드와 복호화된 CVC 정보
     */
    public CardWithCvc getValidatedCardWithCvc(Long cardId, Long userId) {
        UserCard card = userCardRepository.findById(cardId)
                .filter(c -> c.getUserId().equals(userId) && c.getIsActive())
                .orElseThrow(() -> AccountServerException.cardNotFound(cardId));
        String decryptedCvc = getDecryptedCvc(card);

        return CardWithCvc.builder()
                .card(card)
                .cvc(decryptedCvc)
                .build();
    }

    // 내부 클래스 또는 별도 DTO로 분리 가능
    @Data
    @Builder
    @AllArgsConstructor
    public static class CardWithCvc {
        private UserCard card;
        private String cvc;
    }
}