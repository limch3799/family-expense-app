package com.example.d105.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GroupTodayExpenseResponse {
    private Long groupId;
    private String groupName;
    private Long todayExpense;         // 오늘 지출 총액
    private Integer transactionCount;  // 오늘 거래 건수

    public static GroupTodayExpenseResponse of(Long groupId, String groupName,
                                               Long todayExpense, Integer transactionCount) {
        return GroupTodayExpenseResponse.builder()
                .groupId(groupId)
                .groupName(groupName)
                .todayExpense(todayExpense)
                .transactionCount(transactionCount)
                .build();
    }
}