package com.example.d105.ssafy.transaction.dto.request;

import com.example.d105.ssafy.util.CommonHeaderUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SsafyAccountTransactionRequest {

    @JsonProperty("Header")
    private SsafyApiHeader header;

    private String accountNo;
    private String startDate;
    private String endDate;
    private String transactionType;  // A: 전체, 1: 입금, 2: 출금
    private String orderByType;      // ASC: 오름차순, DESC: 내림차순

    public static SsafyAccountTransactionRequest of(String userKey, String apiKey, String accountNo, String startDate, String endDate) {
        SsafyApiHeader header = SsafyApiHeader.builder()
                .apiName("inquireTransactionHistoryList")
                .transmissionDate(CommonHeaderUtil.getCurrentDate())
                .transmissionTime(CommonHeaderUtil.getCurrentTime())
                .institutionCode(CommonHeaderUtil.getInstitutionCode())
                .fintechAppNo(CommonHeaderUtil.getFintechAppNo())
                .apiServiceCode("inquireTransactionHistoryList")
                .institutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos())
                .apiKey(apiKey)
                .userKey(userKey)
                .build();

        return SsafyAccountTransactionRequest.builder()
                .header(header)
                .accountNo(accountNo)
                .startDate(startDate)
                .endDate(endDate)
                .transactionType("A")     // 전체 거래내역
                .orderByType("DESC")      // 최신순
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