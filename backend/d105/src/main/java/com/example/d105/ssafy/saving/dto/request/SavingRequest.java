package com.example.d105.ssafy.saving.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.*;

public class SavingRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
        private String apiKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserHeader {
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
    //적금 계좌 (단건) 상세 조회
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InquireAccountRequest{
        @JsonProperty("Header")
        private UserHeader header;

        private String accountNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InquirePaymentRequest{
        @JsonProperty("Header")
        private UserHeader header;

        private String accountNo;
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateSavingAccountRequest {
        @JsonProperty("Header")
        private UserHeader header;
        private String accountTypeUniqueNo;
        private String depositBalance;
        private String withdrawalAccountNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateSavingRequest {
        @JsonProperty("Header")
        private Header header;

        private String bankCode;
        private String accountName;
        private String accountDescription;
        private String subscriptionPeriod;
        private String minSubscriptionBalance;
        private String maxSubscriptionBalance;
        private String interestRate;
        private String rateDescription;
    }

    //상품 개설시 사용
    @Getter
    public enum SubscriptionPeriod {
        SEVEN_DAYS("7D", "7"),
        ONE_MONTH("1M", "30"),
        THREE_MONTHS("3M", "90"),
        SIX_MONTHS("6M", "180"),
        ONE_YEAR("1Y", "365");

        private final String code;
        private final String days;

        SubscriptionPeriod(String code, String days) {
            this.code = code;
            this.days = days;
        }

        @JsonValue
        public String getCode() {
            return code;
        }

        @JsonCreator
        public static SubscriptionPeriod fromCode(String code) {
            for (SubscriptionPeriod p : values()) {
                if (p.code.equalsIgnoreCase(code)) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Invalid subscription period code: " + code);
        }
    }

    //계좌 생성시에 사용
    @Getter
    public enum SelectSubscripteionPeriod {
        SEVEN_DAYS("7D", "999-3-d19bacb7f24049"),
        ONE_MONTH("1M", "999-3-80d56a778ecc47"),
        THREE_MONTHS("3M", "999-3-cd967eecaea34e"),
        SIX_MONTHS("6M", "999-3-dde180e00dff45"),
        ONE_YEAR("1Y", "999-3-91dc15ebcb7f49");

        private final String code;
        private final String accountTypeUniqueNo;

        SelectSubscripteionPeriod(String code, String accountTypeUniqueNo) {
            this.code = code;
            this.accountTypeUniqueNo = accountTypeUniqueNo;
        }

        @JsonValue
        public String getCode() {
            return code;
        }

        @JsonCreator
        public static SelectSubscripteionPeriod fromCode(String code) {
            for (SelectSubscripteionPeriod p : values()) {
                if (p.code.equalsIgnoreCase(code)) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Invalid subscription period code: " + code);
        }
    }
}