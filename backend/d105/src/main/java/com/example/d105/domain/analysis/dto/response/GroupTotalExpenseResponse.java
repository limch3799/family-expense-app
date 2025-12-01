package com.example.d105.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupTotalExpenseResponse {
    private CurrentPeriod currentPeriod;
    private PreviousPeriod previousPeriod;
    private String changeRate;        // "7.55%" 또는 "신규"
    private Long changeAmount;        // 200000
    private String changeType;        // "INCREASE" or "DECREASE"

    @Getter
    @Builder
    public static class CurrentPeriod {
        private String yearMonth;     // "2025-09"
        private Long totalAmount;     // 2850000
        private Integer transactionCount; // 142
    }

    @Getter
    @Builder
    public static class PreviousPeriod {
        private String yearMonth;     // "2025-08"
        private Long totalAmount;     // 2650000
    }
}