// SpendingTrendResponse.kt
package com.moaga.app.data.api.dto.response

data class SpendingTrendResponse(
    val dailyAmounts: List<DailyAmount>
)

data class DailyAmount(
    val date: String, // "2025-09-19" 형식
    val totalAmount: Int
)