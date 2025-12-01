package com.example.d105.ssafy.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SsafyCardTransactionResponse {

    @JsonProperty("Header")
    private SsafyHeader header;

    @JsonProperty("REC")
    private CardTransactionRec rec;

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
    public static class CardTransactionRec {
        private String cardIssuerCode;
        private String cardIssuerName;
        private String cardName;
        private String cardNo;
        private String estimatedBalance;
        private List<CardTransactionRecord> transactionList;
    }

    @Getter
    @Setter
    public static class CardTransactionRecord {
        private String transactionUniqueNo;
        private String categoryId;           // CG-3fa85f6425e811e 형태
        private String categoryName;         // 주유, 대형마트 등
        private String merchantId;
        private String merchantName;         // SK 에너지 등
        private String transactionDate;
        private String transactionTime;
        private String transactionBalance;   // 거래금액
        private String cardStatus;          // 승인, 취소 등
        private String billStatementsYn;    // Y/N
        private String billStatementsStatus; // 미결제, 결제완료 등
    }
}