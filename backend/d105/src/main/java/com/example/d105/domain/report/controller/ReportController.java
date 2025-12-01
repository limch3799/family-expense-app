package com.example.d105.domain.report.controller;

import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.report.dto.request.ReportRequest;
import com.example.d105.domain.report.dto.response.ReportResponse;
import com.example.d105.domain.report.service.ReportService;
import com.example.d105.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "9.리포트 관리", description = "Report Management")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/members")
    @Operation(
            summary = "리포트 맴버 조회",
            description = "리포트의 기간 및 맴버를 조회합니다."
    )
    public ResponseEntity<ReportResponse.GroupMemberDetailResponse> getMembersDetail(
            @RequestBody ReportRequest.GroupMemberDetailRequest request
    ) {

        return ResponseEntity.ok(reportService.groupMemberDetailResponse(request));
    }

//    @PostMapping("/generate")
//    @Operation(
//            summary = "그룹 별 리포트 생성",
//            description = "그룹 별 리포트를 생성합니다."
//    )
//    public ResponseEntity<ReportResponse.CreateReportResponse> getMembersDetail(
//            @RequestBody  ReportRequest.CreateReportRequest request
//    ) {
//
//        return ResponseEntity.ok(reportService.createAiReport(request));
//    }

    @PostMapping("/detail")
    @Operation(
            summary = "그룹 별 리포트 살세 조회",
            description = "그룹 별 리포트 내용을 상세 조회 합니다."
    )
    public ResponseEntity<ReportResponse.GetReportDetailResponse> getReportDetail(
            @RequestBody  ReportRequest.GetReportDetailRequest request
    ) {

        return ResponseEntity.ok(reportService.getReportDetail(request));
    }

    @PostMapping("/list")
    @Operation(
            summary = "그룹 별 리포트 목록 조회",
            description = "그룹 별 리포트 목록을 조회합니다."
    )
    public ResponseEntity<List<ReportResponse.GetReportListResponse>> getReportList(
            @RequestBody  ReportRequest.GetReportListRequest request
    ) {

        return ResponseEntity.ok(reportService.getReportListResponses(request));
    }
}
