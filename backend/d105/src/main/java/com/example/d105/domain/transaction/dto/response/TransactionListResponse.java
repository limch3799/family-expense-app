package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TransactionListResponse {
    private List<TransactionInfo> transactions;
    private Integer totalElements;
    private Integer totalPages;
    private String message;

    @Getter
    @Builder
    public static class TransactionInfo {
        private Long transactionId;
        private String transactionDate;
        private String transactionTime;
        private String transactionType;
        private Integer amount;
        private Short categoryId;
        private String categoryName;
        private Boolean isExcluded;
        private String description;
    }
}