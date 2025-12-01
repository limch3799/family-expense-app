package com.example.d105.ssafy.account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsafyCardListResponse {

    @JsonProperty("Header")
    private Header header;

    @JsonProperty("REC")
    private List<CardRecord> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String responseCode;
        private String responseMessage;
        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String apiKey;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardRecord {
        private String cardNo;
        private String cvc;
        private String cardUniqueNo;
        private String cardIssuerCode;
        private String cardIssuerName;
        private String cardName;
        private String baselinePerformance;
        private String maxBenefitLimit;
        private String cardDescription;
        private String cardExpiryDate;
        private String withdrawalAccountNo;
        private String withdrawalDate;
    }
}