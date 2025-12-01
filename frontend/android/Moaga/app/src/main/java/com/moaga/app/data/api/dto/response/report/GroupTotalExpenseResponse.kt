package com.moaga.app.data.api.dto.response.report

data class GroupTotalExpenseResponse(
    val currentPeriod: CurrentPeriodResponse,
    val previousPeriod: PreviousPeriodResponse,
    val changeRate: String,
    val changeAmount: Int,
    val changeType: String
)

data class CurrentPeriodResponse(
    val yearMonth: String,
    val totalAmount: Int,
    val transactionCount: Int
)

data class PreviousPeriodResponse(
    val yearMonth: String,
    val totalAmount: Int
)
