package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CategoryChartListResponse {

    private String message;
    private CategoryChartListData data;

    @Getter
    @Setter
    @Builder
    public static class CategoryChartListData {
        private String yearMonth;
        private Short categoryId;
        private String categoryName;
        private Long totalAmount;
        private Integer totalCount;
        private List<TransactionDetail> transactions;
    }

    @Getter
    @Setter
    @Builder
    public static class TransactionDetail {
        private Long transactionId;
        private String transactionDate;
        private String transactionTime;
        private Long amount;
        private String merchantName;
        private Boolean isExcluded;
    }
}