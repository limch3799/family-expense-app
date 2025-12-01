package com.example.d105.ssafy.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SsafyAccountTransactionResponse {

    @JsonProperty("Header")
    private SsafyHeader header;

    @JsonProperty("REC")
    private AccountTransactionRec rec;

    @Getter
    @Setter
    public static class SsafyHeader {
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

    @Getter
    @Setter
    public static class AccountTransactionRec {
        private String totalCount;
        private List<AccountTransactionRecord> list;
    }

    @Getter
    @Setter
    public static class AccountTransactionRecord {
        private String transactionUniqueNo;
        private String transactionDate;
        private String transactionTime;
        private String transactionType;          // 1: 입금, 2: 출금
        private String transactionTypeName;     // 입금, 출금, 출금(이체)
        private String transactionAccountNo;    // 이체 상대방 계좌번호
        private String transactionBalance;      // 거래금액
        private String transactionAfterBalance; // 거래 후 잔액
        private String transactionSummary;      // 거래 요약
        private String transactionMemo;         // 거래 메모
    }
}