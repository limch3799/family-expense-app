package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TransactionDailyResponse {
    private String date;
    private Integer totalAmount;
    private Integer transactionCount;
    private List<DailyTransactionInfo> transactions;

    @Getter
    @Builder
    public static class DailyTransactionInfo {
        private Long transactionId;
        private String transactionTime;
        private Integer amount;
        private String categoryName;
        private Boolean isExcluded;
        private String description;
    }
}