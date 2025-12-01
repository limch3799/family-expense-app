package com.example.d105.domain.account.controller;

import com.example.d105.domain.account.dto.request.AccountGroupConnectRequest;
import com.example.d105.domain.account.dto.request.AccountGroupDisconnectRequest;
import com.example.d105.domain.account.dto.response.*;
import com.example.d105.domain.account.service.AccountService;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.security.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/financial")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "3. 계좌/카드연결", description = "Account Connection")
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationUtil authenticationUtil;

    // 기존 3.1 계좌/카드 목록 조회
    @Operation(
            summary = "계좌/카드 목록 조회",
            description = "계좌와 카드목록을 통합조회"
    )
    @PostMapping("/list")
    public ResponseEntity<AccountListResponse> getAccountAndCardList(Authentication authentication) {


            Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);

            log.info("Processing account and card list request for user: {}", userId);

            AccountListResponse response = accountService.getAccountAndCardList(userId);

            log.info("Successfully processed account and card list for user: {}, accounts: {}, cards: {}",
                    userId, response.getAccounts().size(), response.getCards().size());

            return ResponseEntity.ok(response);


    }

    // 3.2 계좌/카드 그룹 연결
    @Operation(
            summary = "계좌/카드 그룹 연결",
            description = "여러 계좌와 카드를 특정 그룹에 동시 연결"
    )
    @PostMapping("/group/connect")
    public ResponseEntity<AccountGroupConnectResponse> connectAccountsAndCards(
            @RequestBody AccountGroupConnectRequest request,
            Authentication authentication) {


            Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);

            log.info("Processing group connection request for user: {}, group: {}, accounts: {}, cards: {}",
                    userId, request.getGroupId(),
                    request.getAccountIds() != null ? request.getAccountIds().size() : 0,
                    request.getCardIds() != null ? request.getCardIds().size() : 0);

            AccountGroupConnectResponse response = accountService.connectAccountsAndCards(userId, request);

            log.info("Successfully processed group connection for user: {}, message: {}",
                    userId, response.getMessage());

            return ResponseEntity.ok(response);

    }

    // 3.3 계좌/카드 그룹 연결 해제
    @Operation(
            summary = "계좌/카드 그룹 연결 해제",
            description = "하나의 계좌 또는 카드를 그룹에서 해제"
    )
    @PostMapping("/group/disconnect")
    public ResponseEntity<AccountGroupDisconnectResponse> disconnectAccountOrCard(
            @RequestBody AccountGroupDisconnectRequest request,
            Authentication authentication) {

            Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);

            log.info("Processing group disconnection request for user: {}, group: {}, account: {}, card: {}",
                    userId, request.getGroupId(), request.getAccountId(), request.getCardId());

            AccountGroupDisconnectResponse response = accountService.disconnectAccountOrCard(userId, request);

            log.info("Successfully processed group disconnection for user: {}, type: {}, id: {}",
                    userId, response.getDisconnectedType(), response.getDisconnectedId());

            return ResponseEntity.ok(response);


    }

    // 3.4 내 그룹 연결 계좌/카드 목록 조회
    @Operation(
            summary = "내 그룹 연결 계좌/카드 목록 조회",
            description = "특정 그룹에 연결된 내 계좌와 카드 목록만 조회"
    )
    @PostMapping("/group/{groupId}/connected")
    public ResponseEntity<AccountGroupConnectedListResponse> getMyConnectedAccountsAndCards(
            @PathVariable Long groupId,
            Authentication authentication) {


            Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);

            log.info("Processing connected list request for user: {}, group: {}", userId, groupId);

            AccountGroupConnectedListResponse response = accountService.getMyConnectedAccountsAndCards(userId, groupId);

            log.info("Successfully retrieved connected list for user: {}, group: {}, accounts: {}, cards: {}",
                    userId, groupId,
                    response.getMyConnectedAccounts().size(),
                    response.getMyConnectedCards().size());

            return ResponseEntity.ok(response);

    }

}