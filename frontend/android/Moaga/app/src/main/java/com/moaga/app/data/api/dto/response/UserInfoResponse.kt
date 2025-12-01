package com.moaga.app.data.api.dto.response


data class UserInfoResponse(
    val userId: Int,
    val groupId: Int,
    val email: String,
    val username: String,
    val phoneNumber: String,
    val birthDate: String,
    val gender: String,
    val planerPushEnabled: Boolean,
    val reporterPushEnabled: Boolean,
    val transactionPushEnabled: Boolean,
    val createdAt: String
)