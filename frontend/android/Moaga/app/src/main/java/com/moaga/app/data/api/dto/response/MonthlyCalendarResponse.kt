package com.moaga.app.data.api.dto.response

data class MonthlyCalendarResponse(
    val monthlyCalendar: List<MonthlyCalendarItem>
)

data class MonthlyCalendarItem(
    val date: String, // "2025-09-01"
    val totalAmount: Int,
    val transactionCount: Int
)