package com.moaga.app.data.api.dto.request

class EmailRequest {
    data class SendEmailRequest(
        val email: String
    )

    data class VerifyEmailRequest(
        val email: String,
        val code: String
    )
}