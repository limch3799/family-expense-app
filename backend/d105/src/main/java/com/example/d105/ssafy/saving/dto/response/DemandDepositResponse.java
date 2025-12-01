package com.example.d105.ssafy.saving.dto.response;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class DemandDepositResponse {


    @Data
    public static class DemandDepositeInfo{

        @JsonProperty("Header")
        private Header header;
        @JsonProperty("REC")
        private DepositeInfoREC rec;
    }

    //계좌 조회시의 응답
    @Data
    public static class DepositeInfoREC{
    private String bankCode;
    private String bankName;
    private String userName;
    private String accountNo;
    private String accountName;
    private String accountTypeCode;
    private String accountTypeName;
    private String accountCreatedDate;
    private String accountExpiryDate;
    private String dailyTransferLimit;
    private String oneTimeTransferLimit;
    private String accountBalance;
    private String lastTransactionDate;
    private String currency;

    }

    @Data
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
    public static class UpdateDepositRec{
        private String transactionUniqueNo;
        private String transactionDate;
    }

    @Data
    public static class UpdateDepositResponse{
        @JsonProperty("Hedaer")
        private Header header;

        @JsonProperty("REC")
        private UpdateDepositRec rec;

    }



    @Data
    public static class CreateRec{
         private String bankCode;
       private  String accountNo;
       private  Currency currency;

    }
    @Data
    static class Currency{
        private String currency;
        private String currencyName;
    }

    //계좌 생성시의 응답
    @Data
    public static class CreateDepositResponse{
        @JsonProperty("Header")
        private Header header;
        @JsonProperty("REC")
        private CreateRec rec;
    }
}
