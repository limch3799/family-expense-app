package com.moaga.app.data.api.dto.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val simplePassword: String,
    val username: String,
    val phoneNumber: String,
    val birthDate: String,
    val gender: String
)
