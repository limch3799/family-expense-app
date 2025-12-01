package com.moaga.app.data.api.dto.response

data class DailyTransactionsResponse(
    val date: String,
    val groupId: Int,
    val totalAmount: Int,
    val totalCount: Int,
    val transactions: List<TransactionItem>
)

data class TransactionItem(
    val transactionId: Int,
    val transactionTime: String,
    val amount: Int,
    val categoryName: String,
    val isExcluded: Boolean,
    val description: String,
    val memberName: String,
    val realName: String
)