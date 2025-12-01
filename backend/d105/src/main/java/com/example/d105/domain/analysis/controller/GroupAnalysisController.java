package com.example.d105.domain.analysis.controller;

import com.example.d105.domain.analysis.dto.request.*;
import com.example.d105.domain.analysis.dto.response.*;
import com.example.d105.domain.analysis.service.GroupAnalysisService;
import com.example.d105.security.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis/group")
@RequiredArgsConstructor
@Tag(name = "5. 그룹 지출 분석", description = "Group Expense Analysis")
@Slf4j
public class GroupAnalysisController {

    private final GroupAnalysisService groupAnalysisService;
    private final AuthenticationUtil authenticationUtil;

    /**
     * 5.1) 최근 14일 간 지출 총액 변화 조회
     */
    @PostMapping("/recent-trend")
    @Operation(
            summary = "최근 14일 간 지출 총액 변화 조회",
            description = "그룹의 최근 14일간 일별 지출 총액 변화를 차트용으로 조회"
    )
    public ResponseEntity<GroupRecentTrendResponse> getRecentTrend(
            @Valid @RequestBody GroupRecentTrendRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group recent trend request from userId={}, groupId={}", userId, request.getGroupId());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가
        GroupRecentTrendResponse response = groupAnalysisService.getRecentTrend(userId, request.getGroupId());
        return ResponseEntity.ok(response);
    }

    /**
     * 5.1) 그룹 월간 일별 지출 총액 조회 (캘린더용)
     */
    @PostMapping("/monthly-calendar")
    @Operation(
            summary = "그룹 월간 일별 지출 총액 조회",
            description = "그룹의 특정 월 일별 지출 총액을 캘린더 표시용으로 조회"
    )
    public ResponseEntity<GroupMonthlyCalendarResponse> getMonthlyCalendar(
            @Valid @RequestBody GroupMonthlyCalendarRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group monthly calendar request from userId={}, groupId={}, yearMonth={}",
                userId, request.getGroupId(), request.getYearMonth());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가
        GroupMonthlyCalendarResponse response = groupAnalysisService
                .getMonthlyCalendar(userId, request.getGroupId(), request.getYearMonth());
        return ResponseEntity.ok(response);
    }

    /**
     * 5.1) 특정 날짜 그룹 거래내역 조회
     */
    @PostMapping("/daily-transactions")
    @Operation(
            summary = "특정 날짜 그룹 거래내역 조회",
            description = "그룹의 특정 날짜 모든 멤버 거래내역을 상세 조회"
    )
    public ResponseEntity<GroupDailyTransactionsResponse> getDailyTransactions(
            @Valid @RequestBody GroupDailyTransactionsRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group daily transactions request from userId={}, groupId={}, date={}",
                userId, request.getGroupId(), request.getDate());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가
        GroupDailyTransactionsResponse response = groupAnalysisService
                .getDailyTransactions(userId, request.getGroupId(), request.getDate());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/total-expense")
    @Operation(summary = "그룹 총 지출액 및 증감률 조회")
    public ResponseEntity<GroupTotalExpenseResponse> getTotalExpense(
            @Valid @RequestBody GroupTotalExpenseRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group total expense request from userId={}, groupId={}, yearMonth={}",
                userId, request.getGroupId(), request.getYearMonth());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가
        GroupTotalExpenseResponse response = groupAnalysisService
                .getTotalExpense(userId, request.getGroupId(), request.getYearMonth());
        return ResponseEntity.ok(response);
    }

    /**
     * 5.2) 카테고리별 지출 분석
     */
    @PostMapping("/category-analysis")
    @Operation(
            summary = "카테고리별 지출 분석",
            description = "그룹의 특정 월 카테고리별 지출 통계를 차트용으로 조회"
    )
    public ResponseEntity<GroupCategoryAnalysisResponse> getCategoryAnalysis(
            @Valid @RequestBody GroupCategoryAnalysisRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group category analysis request from userId={}, groupId={}, yearMonth={}",
                userId, request.getGroupId(), request.getYearMonth());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가, try-catch 제거
        GroupCategoryAnalysisResponse response = groupAnalysisService
                .getCategoryAnalysis(userId, request.getGroupId(), request.getYearMonth());
        return ResponseEntity.ok(response);
    }

    /**
     * 5.2) 카테고리별 그룹 거래내역 조회
     */
    @PostMapping("/category-transactions")
    @Operation(
            summary = "카테고리별 그룹 거래내역 조회",
            description = "그룹의 특정 월 특정 카테고리의 모든 거래내역을 상세 조회"
    )
    public ResponseEntity<GroupCategoryTransactionsResponse> getCategoryTransactions(
            @Valid @RequestBody GroupCategoryTransactionsRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group category transactions request from userId={}, groupId={}, yearMonth={}, categoryId={}",
                userId, request.getGroupId(), request.getYearMonth(), request.getCategoryId());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가, try-catch 제거
        GroupCategoryTransactionsResponse response = groupAnalysisService
                .getCategoryTransactions(userId, request.getGroupId(), request.getYearMonth(), request.getCategoryId());
        return ResponseEntity.ok(response);
    }

    /**
     * 5.2) 멤버별 지출 분석
     */
    @PostMapping("/member-analysis")
    @Operation(
            summary = "멤버별 지출 분석",
            description = "그룹의 특정 월 멤버별 지출 통계 및 카테고리별 분석을 차트용으로 조회"
    )
    public ResponseEntity<GroupMemberAnalysisResponse> getMemberAnalysis(
            @Valid @RequestBody GroupMemberAnalysisRequest request,
            Authentication authentication) {

        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group member analysis request from userId={}, groupId={}, yearMonth={}",
                userId, request.getGroupId(), request.getYearMonth());

        // ★ 수정: Service에서 권한 검증 처리, userId 파라미터 추가, try-catch 제거
        GroupMemberAnalysisResponse response = groupAnalysisService
                .getMemberAnalysis(userId, request.getGroupId(), request.getYearMonth());
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 오늘 지출 총액 조회
     */
    @PostMapping("/today-expense")
    @Operation(
            summary = "그룹 오늘 지출 총액 조회",
            description = "사용자가 속한 그룹의 오늘 지출 총액 및 거래 건수 조회"
    )
    public ResponseEntity<GroupTodayExpenseResponse> getTodayExpense(Authentication authentication) {
        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group today expense request from userId={}", userId);

        // ★ 수정: try-catch 제거 (GlobalExceptionHandler가 처리)
        GroupTodayExpenseResponse response = groupAnalysisService.getTodayExpense(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 마지막 업데이트 시각 조회
     */
    @PostMapping("/last-updated")
    @Operation(
            summary = "그룹 마지막 업데이트 시각 조회",
            description = "사용자가 속한 그룹의 마지막 업데이트 시각 조회"
    )
    public ResponseEntity<GroupLastUpdatedResponse> getLastUpdated(Authentication authentication) {
        Long userId = authenticationUtil.getUserIdFromAuthentication(authentication);
        log.info("Group last updated request from userId={}", userId);

        // ★ 수정: try-catch 제거 (GlobalExceptionHandler가 처리)
        GroupLastUpdatedResponse response = groupAnalysisService.getLastUpdated(userId);
        return ResponseEntity.ok(response);
    }
}