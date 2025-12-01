package com.example.d105.domain.group.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class SavingPlanResponse {



    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanInfoResponse{
        private Long planId;
        private String title;
        private Integer amount;
        private Integer targetAmount;
        private String createdAt;
        private int dDay;
        private List<Transcation> transcationsList;

    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transcation{
        private Long userId;
        private String userName;
        private Integer amount;
        private String date;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanHistoryResponse{
    List<PlanHistory> plans;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanHistory{
     private Long planId;
     private String title;
     private String createdAt;
     private String completedAt;
     private String status;
    }

}
