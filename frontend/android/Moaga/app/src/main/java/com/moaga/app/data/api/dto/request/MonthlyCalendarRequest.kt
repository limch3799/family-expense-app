package com.moaga.app.data.api.dto.request

data class MonthlyCalendarRequest(
    val groupId: Int,
    val yearMonth: String // "2025-09" 형식
)