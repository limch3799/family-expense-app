package com.example.d105.domain.report.dto.response;

import com.example.d105.domain.group.dto.response.GroupMemberResponse;
import lombok.*;

import java.util.List;

public class ReportResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberDetailResponse{
        private String startDay;
        private String endDay;
        private List<String> names;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReportResponse{
        private String yearMonth;
        private String analysis;
        private String generateAt;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetReportListResponse{
private Long aiReportId;
private String yearMonth;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetReportDetailResponse{
        private Long aiReportId;
        private String yearMonth;
        private String reportContent;
        private String generatedAt;
    }

}
