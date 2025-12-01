package com.example.d105.domain.report.scheduler;

import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.report.service.ReportService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class ReportScheduler {


    private ReportService reportService;


    private GroupRepository groupRepository;

    /**
     * 매달 1일 오전 2시에 전월 리포트 생성
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReports() {
        log.info("=== 월간 리포트 자동 생성 시작 ===");

        // 전월 yearMonth 계산
        String previousYearMonth = getPreviousYearMonth();
        log.info("생성 대상 월: {}", previousYearMonth);

        // 활성화된 모든 그룹 조회
        List<Long> activeGroupIds = groupRepository.findGroupIdsByDeletedAtIsNull();
        log.info("대상 그룹 수: {}", activeGroupIds.size());

        // 각 그룹별로 비동기 리포트 생성
        int successCount = 0;
        for (Long groupId : activeGroupIds) {
            try {
                reportService.generateReportAsync(groupId, previousYearMonth);
                successCount++;
            } catch (Exception e) {
                log.error("그룹 ID {}의 리포트 스케줄링 실패", groupId, e);
            }
        }

        log.info("=== 월간 리포트 스케줄링 완료: {}/{} ===", successCount, activeGroupIds.size());
    }

    /**
     * 전월 yearMonth 계산 (YYYY-MM 형식)
     */
    private String getPreviousYearMonth() {
        LocalDate now = LocalDate.now();
        LocalDate previousMonth = now.minusMonths(1);
        return previousMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

}
