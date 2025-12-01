package com.example.d105.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class GroupRecentTrendResponse {
    private List<DailyAmount> dailyAmounts;

    @Getter
    @Builder
    public static class DailyAmount {
        private String date;        // "2025-09-01"
        private Long totalAmount;   // 125000
    }
}