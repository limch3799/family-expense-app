// TransactionRecentTrendResponse.java
package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TransactionRecentTrendResponse {
    private List<DailyAmount> dailyAmounts;

    @Getter
    @Builder
    public static class DailyAmount {
        private String date;        // "2025-09-01"
        private Long totalAmount;   // 50000
    }
}