package com.example.d105.ssafy.transaction.dto.request;

import com.example.d105.ssafy.util.CommonHeaderUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SsafyCardTransactionRequest {

    @JsonProperty("Header")
    private SsafyApiHeader header;

    private String cardNo;
    private String cvc;
    private String startDate;
    private String endDate;

    public static SsafyCardTransactionRequest of(String userKey, String apiKey, String cardNo, String cvc, String startDate, String endDate) {
        SsafyApiHeader header = SsafyApiHeader.builder()
                .apiName("inquireCreditCardTransactionList")
                .transmissionDate(CommonHeaderUtil.getCurrentDate())
                .transmissionTime(CommonHeaderUtil.getCurrentTime())
                .institutionCode(CommonHeaderUtil.getInstitutionCode())
                .fintechAppNo(CommonHeaderUtil.getFintechAppNo())
                .apiServiceCode("inquireCreditCardTransactionList")
                .institutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos())
                .apiKey(apiKey)
                .userKey(userKey)
                .build();

        return SsafyCardTransactionRequest.builder()
                .header(header)
                .cardNo(cardNo)
                .cvc(cvc)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Getter
    @Builder
    public static class SsafyApiHeader {
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
}