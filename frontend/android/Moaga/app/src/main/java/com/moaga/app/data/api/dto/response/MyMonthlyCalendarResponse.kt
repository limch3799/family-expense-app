package com.moaga.app.data.api.dto.response


data class MyMonthlyCalendarResponse(
    val monthlyCalendar: List<MyMonthlyCalendarItem>
)

data class MyMonthlyCalendarItem(
    val date: String,
    val totalAmount: Int,
    val transactionCount: Int
)

data class MyDailyTransactionsResponse(
    val date: String,
    val totalAmount: Int,
    val transactionCount: Int,
    val transactions: List<MyTransactionItem>
)

data class MyTransactionItem(
    val transactionId: Int,
    val transactionTime: String,
    val amount: Int,
    val categoryName: String,
    val isExcluded: Boolean,
    val description: String
)

data class MySpendingTrendResponse(
    val dailyAmounts: List<MyDailyAmountItem>
)

data class MyDailyAmountItem(
    val date: String,
    val totalAmount: Int
)