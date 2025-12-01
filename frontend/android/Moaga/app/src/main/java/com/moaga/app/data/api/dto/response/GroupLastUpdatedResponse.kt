package com.moaga.app.data.api.dto.response

data class GroupLastUpdatedResponse(
    val groupId: Long,
    val groupName: String,
    val lastUpdated: String
)