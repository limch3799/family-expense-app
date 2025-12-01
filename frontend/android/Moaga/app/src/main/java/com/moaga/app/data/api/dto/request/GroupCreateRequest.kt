package com.moaga.app.data.api.dto.request

data class GroupCreateRequest(
    val name: String,
    val description: String,
    val imgId: Long = 1073741824 // 서버에서 무시되므로 기본값 넣어둠
)