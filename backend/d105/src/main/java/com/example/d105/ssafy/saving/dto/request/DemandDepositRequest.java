package com.example.d105.ssafy.saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DemandDepositRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateDemandDepositeRequest{

        @JsonProperty("Header")
        private UserHeader header;

        private String accountTypeUniqueNo;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetDemandDepositeRequest{

        @JsonProperty("Header")
        private UserHeader header;

        private String accountNo;

    }








    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserHeader{

        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
        private String apiKey;
        private String userKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header{

        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
        private String apiKey;
        private String userKey;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateDemandDepositAccountRequest{

        @JsonProperty("Header")
        private UserHeader header;

        private String accountNo;
        private Long transactionBalance;
        private String transactionSummary;


    }


}
