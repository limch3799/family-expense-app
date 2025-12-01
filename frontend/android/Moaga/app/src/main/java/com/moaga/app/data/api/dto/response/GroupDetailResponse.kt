package com.moaga.app.data.api.dto.response

data class GroupDetailResponse(
    val savingAccountNo: String,
    val inviteCode: String,
    val amount: Long,
    val imgId: String?,
    val planId: Int,
    val joinedMembers: List<Member>,
    val waitingMember: List<Member>,
    val createdAt: String
) {
    data class Member(
        val memberId: Int,
        val userId: Int,
        val username: String,
        val displayname: String
    )
}