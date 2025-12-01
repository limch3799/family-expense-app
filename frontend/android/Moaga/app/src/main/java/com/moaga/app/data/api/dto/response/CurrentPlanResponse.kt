package com.moaga.app.data.api.dto.response

import com.google.gson.annotations.SerializedName

data class CurrentPlanResponse(
    val planId: Long,
    val title: String,
    val amount: Long,
    val targetAmount: Long,
    val createdAt: String,
    @SerializedName("transcationsList") val transactionsList: List<PlanTransaction>,
    val dday: Int
) {
    data class PlanTransaction(
        val userId: Int,
        val userName: String,
        val amount: Int,
        val date: String
    )
}
