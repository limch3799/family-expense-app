package com.example.d105.ssafy.saving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class SavingResponse {


    @Data
    public static class Header  {

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
    public static class CreateSavingAccountRec{
        private String bankCode;
        private String bankName;
        private String accountNo;
        private String withdrawalBankCode;
        private String withdrawalAccountNo;
        private String accountName;
        private String interestRate;
        private String subscriptionPeriod;
        private String depositBalance;
        private String accountCreateDate;
        private String accountExpiryDate;
    }

    //적금 계좌(단건) 상세 조회
    @Data
    public static class InquireAccountResponse{
        @JsonProperty("Header")
        private Header header;

        @JsonProperty("REC")
        private InquireAccountRec rec;
    }

    //적금 계좌(단건) 상세 조회
    @Data
    public static class InquireAccountRec{
        private String bankCode;
        private String bankName;
        private String userName;
        private String accountNo;
        private String accountName;
        private String accountDescription;
        private String withdrawalBankCode;
        private String withdrawalAccountNo;
        private String subscriptionPeriod;
        private String depositBalance;
        private String interestRate;
        private String installmentNumber;
        private String totalBalance;
        private String  accountCreateDate;
        private String accountExpiryDate;
    }

    @Data
    public static class CreateSavingAccountResponse{
        @JsonProperty("Header")
        private Header header;

        @JsonProperty("REC")
        private CreateSavingAccountRec rec;
    }

    @Data
public static class CreateSavingResponse{
    @JsonProperty("Header")
        private Header header;
    @JsonProperty("REC")
        private CreateRec rec;

    }

    @Data
    public static class CreateRec{
        private String accountTypeUniqueNo;
        private String bankCode;
        private String bankName;
        private String accountTypeCode;
        private String accountTypeName;
        private String accountName;
        private String accountDescription;
        private String subscriptionPeriod;
        private String minSubscriptionBalance;
        private String maxSubscriptionBalance;
        private String interestRate;
        private String rateDescription;
    }

    @Data
    public static class PaymentInfo{
        private String depositInstallment;
        private String paymentBalance;
        private String paymentDate;
        private String paymentTime;
        private String status;
        private String failureReason;
    }

    @Data
    public static class PaymentRec{
        private String bankCode;
        private String bankName;
        private String accountNo;
        private String accountName;
        private String interestRate;
        private String depositBalance;
        private String totalBalance;
        private String accountCreateDate;
        private String accountExpiryDate;
        @JsonProperty("paymentInfo")
        private List<PaymentInfo> infos;
    }

    @Data
    public static class PaymentResponse{
        @JsonProperty("Header")
        private Header header;
        @JsonProperty("REC")
        private List<PaymentRec> rec;

    }

}
