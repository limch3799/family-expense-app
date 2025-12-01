package com.moaga.app.data.api.dto.response

data class GroupInfoResponse(
    val groupId: Long,
    val groupName: String,
    val groupDescription: String,
    val savingAccountNo: String,
    val inviteCode: String,
    val amount: Int,
    val imgId: String?,
    val planId: Int,
    val joinedMembers: List<GroupMember>,
    val waitingMember: List<GroupMember>,
    val createdAt: String
)


data class GroupMember(
    val memberId: Int,
    val userId: Int,
    val username: String,
    val displayname: String
)