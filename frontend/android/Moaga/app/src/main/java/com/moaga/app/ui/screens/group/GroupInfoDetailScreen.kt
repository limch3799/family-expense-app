package com.moaga.app.ui.screens.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moaga.app.R
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.GroupMemberActionRequest
import com.moaga.app.data.api.dto.request.GroupOwnerRequest
import com.moaga.app.data.api.dto.response.GroupInfoResponse
import com.moaga.app.ui.components.expense.getProfileImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoDetailScreen(navController: NavController) {
    val scope = rememberCoroutineScope()

    var groupInfo by remember { mutableStateOf<GroupInfoResponse?>(null) }
    var isOwner by remember { mutableStateOf(false) }
    var currentGroupId by remember { mutableStateOf<Long?>(null) }

    // API 호출
    LaunchedEffect(Unit) {
        try {
            val userInfo = ApiClient.apiService.getUserInfo()
            val gid = userInfo.groupId ?: return@LaunchedEffect
            currentGroupId = gid.toLong()

            val res = ApiClient.apiService.getGroupInfo(mapOf("groupId" to gid))
            groupInfo = res

            val ownerRes = ApiClient.apiService.checkGroupOwner(GroupOwnerRequest(groupId = gid))
            isOwner = ownerRes
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("그룹 정보", fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFEAF6F2)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupInfo?.let { info ->
                // 그룹 기본 정보 카드
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = info.groupName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                            Text(
                                text = info.groupDescription ?: "그룹 설명이 없습니다.",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "인원 ${info.joinedMembers.size}명",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // 그룹장
                if (info.joinedMembers.isNotEmpty()) {
                    val leader = info.joinedMembers.first()
                    item {
                        SectionCard("그룹장") {
                            MemberRow(leader.displayname)
                        }
                    }
                }

                // 그룹원
                if (info.joinedMembers.size > 1) {
                    item {
                        SectionCard("그룹원") {
                            info.joinedMembers.drop(1).forEach { member ->
                                MemberRow(member.displayname)
                            }
                        }
                    }
                }

                // 가입 신청 중
                if (info.waitingMember.isNotEmpty()) {
                    item {
                        SectionCard("가입 신청 중") {
                            info.waitingMember.forEach { member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MemberRow(member.displayname)
                                    if (isOwner) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SmallButton("수락", Color(0xFF18A87E)) {
                                                scope.launch {
                                                    val res = ApiClient.apiService.approveMember(
                                                        GroupMemberActionRequest(
                                                            groupId = currentGroupId ?: return@launch,
                                                            memberId = member.memberId.toLong()
                                                        )
                                                    )
                                                    if (res) {
                                                        groupInfo = groupInfo?.copy(
                                                            waitingMember = groupInfo!!.waitingMember.filterNot {
                                                                it.memberId == member.memberId
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            SmallButton("거절", Color(0xFFF44336)) {
                                                scope.launch {
                                                    val res = ApiClient.apiService.rejectMember(
                                                        GroupMemberActionRequest(
                                                            groupId = currentGroupId ?: return@launch,
                                                            memberId = member.memberId.toLong()
                                                        )
                                                    )
                                                    if (res) {
                                                        groupInfo = groupInfo?.copy(
                                                            waitingMember = groupInfo!!.waitingMember.filterNot {
                                                                it.memberId == member.memberId
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF18A87E))
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SmallButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Text(text, fontSize = 13.sp, color = Color.White)
    }
}

@Composable
fun MemberRow(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFEAF6F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = getProfileImage(name)),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111111))
    }
}
