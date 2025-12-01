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
public class GroupCategoryAnalysisResponse {

    private String yearMonth;
    private Long totalAmount;
    private List<CategoryExpense> categoryExpenses;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpense {
        private Short categoryId;
        private String categoryName;
        private Long amount;
        private Double percentage;
        private Integer transactionCount;
    }
}