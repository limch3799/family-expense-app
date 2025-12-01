package com.example.d105.domain.transaction.controller;

import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.transaction.dto.request.*;
import com.example.d105.domain.transaction.dto.response.*;
import com.example.d105.domain.transaction.service.TransactionService;
import com.example.d105.domain.transaction.service.TransactionQueryService;
import com.example.d105.security.dto.CustomUserDetails;
import com.example.d105.security.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "4. 거래내역 관리", description = "Transaction Management")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionQueryService transactionQueryService;
    private final AuthenticationUtil authenticationUtil;

    /**
     * 4.1 거래내역 수집 - 수동 동기화
     */
    @PostMapping("/transactions/sync")
    @Operation(
            summary = "거래내역 수집",
            description = "사용자 요청 기반 거래 내역 증분 업데이트"
    )
    public ResponseEntity<TransactionSyncResponse> syncTransactions(Authentication authentication) {
        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Transaction sync request from userId={}", userId);

        Map<String, Object> result = transactionService.syncAllTransactions(userId);

        TransactionSyncResponse response = TransactionSyncResponse.builder()
                .message((String) result.get("message"))
                .newTransactionCount((Integer) result.get("newTransactionCount"))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 4.2 카테고리 목록 조회
     */
    @Operation(
            summary = "카테고리 목록 조회",
            description = "카테고리 목록 조회"
    )
    @PostMapping("/categories/list")
    public ResponseEntity<CategoryListResponse> getCategoryList(Authentication authentication) {
        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Category list request from userId={}", userId);

        CategoryListResponse response = transactionQueryService.getCategoryList();
        return ResponseEntity.ok(response);
    }

    /**
     * 4.2 거래내역 카테고리 변경
     */
    @Operation(
            summary = "거래내역 카테고리 변경",
            description = "특정 거래 내역의 카테고리 변경"
    )
    @PostMapping("/transactions/category/change")
    public ResponseEntity<Map<String, String>> changeTransactionCategory(
            @Valid @RequestBody TransactionCategoryChangeRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Transaction category change request from userId={}, transactionId={}, newCategoryId={}",
                userId, request.getTransactionId(), request.getCategoryId());

        transactionQueryService.changeTransactionCategory(userId, request.getTransactionId(), request.getCategoryId());

        return ResponseEntity.ok(Map.of("message", "거래내역 카테고리가 변경되었습니다."));
    }

    /**
     * 4.3 개인 거래내역 조회 (월별 페이징)
     */
    @Operation(
            summary = "개인 거래내역 조회 (월별 페이징)",
            description = "개인 거래내역 조회"
    )
    @PostMapping("/transactions/my/list")
    public ResponseEntity<TransactionListResponse> getMyTransactionList(
            @Valid @RequestBody TransactionListRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Transaction list request from userId={}, yearMonth={}, page={}",
                userId, request.getYearMonth(), request.getPage());

        TransactionListResponse response = transactionQueryService
                .getTransactionList(userId, request.getYearMonth(), request.getPage(), request.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * 4.3 개인 일별 거래내역 조회
     */
    @Operation(
            summary = "개인 일별 거래내역 조회",
            description = "개인 일별 지출 상세 내역 조회"
    )
    @PostMapping("/transactions/my/daily")
    public ResponseEntity<TransactionDailyResponse> getDailyTransactions(
            @Valid @RequestBody TransactionDailyRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Daily transaction request from userId={}, date={}", userId, request.getDate());

        TransactionDailyResponse response = transactionQueryService
                .getDailyTransactions(userId, request.getDate());

        return ResponseEntity.ok(response);
    }

    /**
     * 4.3 최근 14일간 지출 총액 변화 조회
     */
    @Operation(
            summary = "최근 14일간 지출 총액 변화 조회",
            description = "최근 14일 간 지출 총액 변화 조회"
    )
    @PostMapping("/transactions/my/recent-trend")
    public ResponseEntity<TransactionRecentTrendResponse> getRecentTrend(Authentication authentication) {
        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Recent trend request from userId={}", userId);

        TransactionRecentTrendResponse response = transactionQueryService.getRecentTrend(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4.3 개인 월간 일별 지출 총액 조회 (캘린더용)
     */
    @Operation(
            summary = "개인 월간 일별 지출 총액 조회 (캘린더용)",
            description = "개인 월간 일별 지출 총액 조회 (캘린더용)"
    )
    @PostMapping("/transactions/my/monthly-calendar")
    public ResponseEntity<TransactionMonthlyCalendarResponse> getMonthlyCalendar(
            @Valid @RequestBody TransactionMonthlyCalendarRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Monthly calendar request from userId={}, yearMonth={}",
                userId, request.getYearMonth());

        TransactionMonthlyCalendarResponse response = transactionQueryService
                .getMonthlyCalendar(userId, request.getYearMonth());

        return ResponseEntity.ok(response);
    }

    /**
     * 4.4 거래내역 제외 처리
     */
    @Operation(
            summary = "거래내역 제외 처리",
            description = "특정 거래내역 제외 처리"
    )
    @PostMapping("/transactions/exclude")
    public ResponseEntity<Map<String, String>> excludeTransaction(
            @Valid @RequestBody TransactionExcludeRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Transaction exclude request from userId={}, transactionId={}",
                userId, request.getTransactionId());

        transactionQueryService.excludeTransaction(userId, request.getTransactionId());

        return ResponseEntity.ok(Map.of("message", "거래내역이 분석에서 제외되었습니다."));
    }

    /**
     * 4.4 거래내역 포함 처리
     */
    @Operation(
            summary = "거래내역 포함 처리",
            description = "특정 거래내역 제외 해제"
    )
    @PostMapping("/transactions/include")
    public ResponseEntity<Map<String, String>> includeTransaction(
            @Valid @RequestBody TransactionIncludeRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Transaction include request from userId={}, transactionId={}",
                userId, request.getTransactionId());

        transactionQueryService.includeTransaction(userId, request.getTransactionId());

        return ResponseEntity.ok(Map.of("message", "거래내역이 분석에 포함되었습니다."));
    }

    /**
     * 4.5 개인 월간 카테고리별 파이차트
     */
    @Operation(
            summary = "개인 월간 카테고리별 파이차트",
            description = "개인 월간 카테고리별 파이차트 데이터 조회"
    )
    @PostMapping("/transactions/my/category-chart")
    public ResponseEntity<CategoryChartResponse> getCategoryChart(
            @RequestBody CategoryChartRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Category chart request from userId={}, yearMonth={}", userId, request.getYearMonth());

        CategoryChartResponse response = transactionQueryService
                .getCategoryChart(userId, request.getYearMonth());

        return ResponseEntity.ok(response);
    }

    /**
     * 4.5 개인 카테고리별 거래내역 조회
     */
    @Operation(
            summary = "개인 카테고리별 거래내역 조회",
            description = "개인 카테고리별 거래내역 상세 조회"
    )
    @PostMapping("/transactions/my/category-chart-list")
    public ResponseEntity<CategoryChartListResponse> getCategoryChartList(
            @RequestBody CategoryChartListRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Category chart list request from userId={}, yearMonth={}, categoryId={}",
                userId, request.getYearMonth(), request.getCategoryId());

        CategoryChartListResponse response = transactionQueryService
                .getCategoryChartList(userId, request.getYearMonth(), request.getCategoryId());

        return ResponseEntity.ok(response);
    }



}