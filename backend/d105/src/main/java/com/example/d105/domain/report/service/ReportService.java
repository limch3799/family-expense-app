package com.example.d105.domain.report.service;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.report.dto.request.ReportRequest;
import com.example.d105.domain.report.dto.response.ReportResponse;
import com.example.d105.domain.report.entity.AiReport;
import com.example.d105.domain.report.exception.ReportException;
import com.example.d105.domain.report.repository.AiReporterRepository;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.read.entity.GroupMonthlyExpenseStatus;
import com.example.d105.read.repository.GroupMonthlyExpenseStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final GroupMonthlyExpenseStatusRepository groupMonthlyExpenseStatusRepository;
    private final SsafyAiService ssafyAiService;
    private final AiReporterRepository reportRepository;
    private final GroupRepository groupRepository;

    // ========== 기존 메서드들 ==========

    public ReportResponse.GroupMemberDetailResponse groupMemberDetailResponse(ReportRequest.GroupMemberDetailRequest request){
        List<String> members = new ArrayList<>();
        List<GroupMember> groupMemberList = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(request.getGroupId());
        for(GroupMember member : groupMemberList){
            if(member.getExitedAt() == null){
                members.add(userService.getUserName(member.getUser().getUserId()));
            }
        }

        YearMonth yearMonth = YearMonth.of(request.getYear(), request.getMonth());
        LocalDate startDay = yearMonth.atDay(1);
        LocalDate endDay = yearMonth.atEndOfMonth();

        ReportResponse.GroupMemberDetailResponse response = new ReportResponse.GroupMemberDetailResponse();
        response.setNames(members);
        response.setStartDay(startDay.toString());
        response.setEndDay(endDay.toString());

        return response;
    }

    @Transactional
    public ReportResponse.CreateReportResponse createAiReport(ReportRequest.CreateReportRequest request){

        System.out.println("=========================================");
        String yearMonth = request.getYearMonth();
        System.out.println("분석하려는 정보");
        System.out.println("groupId : "+request.getGroupId());
        System.out.println("yearMonth : " + request.getYearMonth());

        List<GroupMonthlyExpenseStatus> currentMonth = groupMonthlyExpenseStatusRepository
                .findByGroupIdAndYearMonthOrderByTotalExpenseDesc(request.getGroupId(), yearMonth);

        if(currentMonth.size() == 0 || currentMonth.isEmpty()){
            throw new ReportException("EXPENSE_RECORD_NOT_FOUND", "해당 월의 지출 기록이 존재하지 않습니다.");
        }

        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        String analysis = generateMonthlyAnalysis(currentMonth, year, month);

        ReportResponse.CreateReportResponse response = new ReportResponse.CreateReportResponse();
        response.setYearMonth(yearMonth);
        response.setAnalysis(analysis);
        response.setGenerateAt(LocalDateTime.now().toString());

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND", "해당 그룹 찾을 수 없음"));

        AiReport report = new AiReport();
        report.setGroup(group);
        report.setYearMonth(request.getYearMonth());
        report.setReportContent(analysis);
        reportRepository.save(report);

        return response;
    }

    public List<ReportResponse.GetReportListResponse> getReportListResponses(ReportRequest.GetReportListRequest request){
        List<AiReport> list = reportRepository.findByGroup_GroupId(request.getGroupId());
        List<ReportResponse.GetReportListResponse> responseList = new ArrayList<>();
        for(AiReport report : list){
            ReportResponse.GetReportListResponse response = new ReportResponse.GetReportListResponse();
            response.setAiReportId(report.getId());
            response.setYearMonth(report.getYearMonth());
            responseList.add(response);
        }
        return responseList;
    }

    public ReportResponse.GetReportDetailResponse getReportDetail(ReportRequest.GetReportDetailRequest request){
        AiReport report = reportRepository.findById(request.getId())
                .orElseThrow(() -> new ReportException("NOT_FOUNT_REPORT", "해당 리포트 찾을 수 없음"));

        ReportResponse.GetReportDetailResponse response = new ReportResponse.GetReportDetailResponse();
        response.setAiReportId(report.getId());
        response.setYearMonth(report.getYearMonth());
        response.setReportContent(report.getReportContent());
        response.setGeneratedAt(report.getGeneratedAt().toString());

        return response;
    }

    // ========== 스케줄러용 비동기 메서드 추가 ==========

    /**
     * 비동기로 AI 리포트 생성 (스케줄러에서 호출)
     */
    @Async("reportTaskExecutor")
    @Transactional
    public CompletableFuture<Void> generateReportAsync(Long groupId, String yearMonth) {
        try {
            log.info("리포트 생성 시작 - 그룹 ID: {}, 대상 월: {}", groupId, yearMonth);

            // 중복 생성 방지
            if (reportRepository.existsByGroup_GroupIdAndYearMonth(groupId, yearMonth)) {
                log.warn("이미 존재하는 리포트 - 그룹 ID: {}, 월: {}", groupId, yearMonth);
                return CompletableFuture.completedFuture(null);
            }

            ReportRequest.CreateReportRequest request = new ReportRequest.CreateReportRequest();
            request.setGroupId(groupId);
            request.setYearMonth(yearMonth);

            createAiReport(request);

            log.info("리포트 생성 완료 - 그룹 ID: {}, 대상 월: {}", groupId, yearMonth);

        } catch (ReportException e) {
            log.warn("리포트 생성 실패 (예상된 오류) - 그룹 ID: {}, 사유: {}", groupId, e.getMessage());
        } catch (Exception e) {
            log.error("리포트 생성 중 예외 발생 - 그룹 ID: {}", groupId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // ========== Private 메서드들 ==========

    private String generateMonthlyAnalysis(List<GroupMonthlyExpenseStatus> currentMonth,
                                           Integer year, Integer month) {
        String analysisPrompt = createRecommendationsPrompt(currentMonth, year, month);
        System.out.println("prompt : " + analysisPrompt);

        String response = ssafyAiService.callSsafyGpt(analysisPrompt, "Answer in Korean");
        System.out.println("Sucess api result !!!!!!!!!!!!-----------------------");
        System.out.println("SSAFY AI 응답 성공!");

        return response;
    }

    private String createRecommendationsPrompt(List<GroupMonthlyExpenseStatus> currentMonth,
                                               Integer year, Integer month) {
        YearMonth current = YearMonth.of(year, month);
        YearMonth next = current.plusMonths(1);

        Integer nextYear = next.getYear();
        Integer nextMonth = next.getMonthValue();

        Long totalExpense = currentMonth.stream()
                .mapToLong(GroupMonthlyExpenseStatus::getTotalExpense)
                .sum();

        Integer totalTransactions = currentMonth.stream()
                .mapToInt(GroupMonthlyExpenseStatus::getTransactionCount)
                .sum();

        GroupMonthlyExpenseStatus topExpense = currentMonth.stream()
                .max((a, b) -> Long.compare(a.getTotalExpense(), b.getTotalExpense()))
                .get();

        Integer memberCount = currentMonth.get(0).getMemberCount();

        StringBuilder currentDataString = new StringBuilder();
        for(GroupMonthlyExpenseStatus expense : currentMonth) {
            currentDataString.append(String.format(
                    "- %s: %,d원 (%.1f%%, 거래 %d건)\n",
                    expense.getCategoryName(),
                    expense.getTotalExpense(),
                    expense.getExpensePercentage().doubleValue(),
                    expense.getTransactionCount()
            ));
        }

        System.out.println("그룹의 지출들 " + currentDataString.toString());

        return String.format("""
당신은 전문 그룹 재정 관리 컨설턴트입니다. 다음 그룹 지출 데이터를 종합적으로 분석하여 
%d년 %d월 분석 리포트와 %d년 %d월 개선 가이드를 하나의 완성된 리포트로 작성해주세요.

## 📋 %d년 %d월 그룹 지출 데이터
- 그룹 총 지출액: %,d원
- 그룹 멤버 수: %d명
- 총 거래건수: %d건
- 1인당 평균 지출: %,d원
- 최대 지출 카테고리: %s (%,d원, %.1f%%)

### 📊 카테고리별 상세 지출:
%s

---

##  %d년 %d월 지출 분석

### 분석 (3줄) \s
1. 이번 달 그룹의 총 지출 규모와 1인당 부담 수준 평가 \s
2. 카테고리별 지출 비중을 비교해 과다·적정 항목 판별 \s
3. 거래 패턴을 통해 그룹의 소비 습관과 협력 수준 진단 \s

##  %d년 %d월 스마트 지출 가이드
### 개선 가이드 (2줄) \s
1. 최대 지출 카테고리 절감 방안과 실천 가능한 절약 전략 제안 \s
2. 다음 달 지출 목표와 장기적인 그룹 재정 관리 방향 제시 \s
                        
※ 한국의 그룹 생활 문화를 고려하여, 구성원 모두가 쉽게 이해하고 동참할 수 있도록 친근하고 실용적으로 작성해주세요.
""",
                year, month, nextYear, nextMonth,
                year, month,
                totalExpense,
                memberCount,
                totalTransactions,
                totalExpense / memberCount,
                topExpense.getCategoryName(),
                topExpense.getTotalExpense(),
                topExpense.getExpensePercentage().doubleValue(),
                currentDataString.toString(),
                year, month,
                nextYear, nextMonth,
                topExpense.getCategoryName(),
                nextYear, nextMonth
        );
    }
}
