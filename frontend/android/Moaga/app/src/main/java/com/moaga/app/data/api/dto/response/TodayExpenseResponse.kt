package com.moaga.app.data.api.dto.response

data class TodayExpenseResponse(
    val groupId: Int,
    val groupName: String,
    val todayExpense: Int,
    val transactionCount: Int
)