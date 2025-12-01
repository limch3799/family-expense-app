package com.moaga.app.data.api.dto.response

data class InvitationInfoResponse(
    val savingsAccountNo: String,
    val name: String,
    val description: String,
    val ownerName: String,
    val memberCount: Int
)