package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TransactionMonthlyCalendarResponse {
    private List<CalendarDay> monthlyCalendar;

    @Getter
    @Builder
    public static class CalendarDay {
        private String date;             // "2025-09-01"
        private Long totalAmount;        // 50000
        private Integer transactionCount; // 5
    }
}