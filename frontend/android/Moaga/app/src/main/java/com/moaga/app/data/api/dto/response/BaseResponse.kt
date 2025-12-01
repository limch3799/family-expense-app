package com.moaga.app.data.api.dto.response

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val code: Int,
    val timestamp: String? = null
)