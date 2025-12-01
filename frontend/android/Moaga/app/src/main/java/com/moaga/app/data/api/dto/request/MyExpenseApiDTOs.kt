package com.moaga.app.data.api.dto.request



data class MyMonthlyCalendarRequest(
    val yearMonth: String
)

data class MyDailyTransactionsRequest(
    val date: String
)