package com.example.d105.domain.group.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SavingPlanRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePlanRequest {

        private String planTitle;
     private Long groupId;
     private Integer targetAmount;
     private String subscribePeriod;
     // 가입 금액
     private String depositBalance;
     

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanInfoRequest {

       private Long planId;


    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanStopRequest {

        private Long planId;


    }



}
