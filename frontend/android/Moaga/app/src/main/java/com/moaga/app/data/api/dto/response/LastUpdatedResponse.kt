package com.moaga.app.data.api.dto.response

data class LastUpdatedResponse(
    val groupId: Int,
    val groupName: String,
    val lastUpdated: String // "2025-09-19 09:28:28" 형식
)