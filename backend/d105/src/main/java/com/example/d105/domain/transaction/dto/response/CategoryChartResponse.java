package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CategoryChartResponse {

    private String message;
    private CategoryChartData data;

    @Getter
    @Setter
    @Builder
    public static class CategoryChartData {
        private String yearMonth;
        private Long totalAmount;
        private Integer totalTransactionCount;
        private List<CategoryStats> categoryStats;
    }

    @Getter
    @Setter
    @Builder
    public static class CategoryStats {
        private Short categoryId;
        private String categoryName;
        private Long amount;
        private Integer transactionCount;
        private Double percentage;
    }
}