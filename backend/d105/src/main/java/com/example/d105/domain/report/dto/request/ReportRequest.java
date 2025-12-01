package com.example.d105.domain.report.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

public class ReportRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberDetailRequest {
        private Integer year;
        private Integer month;
        private Long groupId;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    public static class CreateReportRequest{
        private Long groupId;
        private String yearMonth;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetReportListRequest{
        private Long groupId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetReportDetailRequest{
        private Long id;
    }


}
