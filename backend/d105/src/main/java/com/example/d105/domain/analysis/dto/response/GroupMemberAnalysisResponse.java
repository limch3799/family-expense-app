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
public class GroupMemberAnalysisResponse {

    private String yearMonth;
    private List<MemberAnalysis> memberAnalysis;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberAnalysis {
        private Long userId;
        private String memberName;
        private Long totalAmount;
        private List<CategoryBreakdown> categoryBreakdown;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private Short categoryId;
        private String categoryName;
        private Long amount;
        private Double percentage;
    }
}