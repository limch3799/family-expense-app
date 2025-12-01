package com.example.d105.domain.analysis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCategoryTransactionsResponse {

    private String yearMonth;
    private Short categoryId;
    private String categoryName;
    private Long totalAmount;
    private Integer totalCount;
    private List<TransactionDetail> transactions;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetail {
        private Long transactionId;
        private String transactionDate;
        private String transactionTime;
        private Long amount;
        private String merchantName;
        private String memberName;
        private String realName;
        private Boolean isExcluded;
    }
}