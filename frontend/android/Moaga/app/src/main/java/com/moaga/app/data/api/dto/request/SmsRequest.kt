package com.moaga.app.data.api.dto.request

data class SmsSendRequest(
    val phoneNumber: String
)

data class SmsVerifyRequest(
    val phoneNumber: String,
    val code: String
)