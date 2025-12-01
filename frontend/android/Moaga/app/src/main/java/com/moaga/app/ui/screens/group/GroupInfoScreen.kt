// file: app/src/main/java/com/d105/app/ui/screens/group/GroupInfoScreen.kt
package com.moaga.app.ui.screens.group

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.GroupJoinRequest
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.launch

data class GroupInfo(
    val name: String,
    val description: String,
    val ownerName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    inviteCode: String,
    name: String = "민준이네 가족",
    description: String = "민준이네 가족입니다.",
    ownerName: String = "김민준",
    onBack: () -> Unit = {},
    onJoined: () -> Unit = {}
) {
    val brandBlue  = Color(0xFF18A87E)
    val cardFill   = Color(0xFFF8FAFF)
    val cardStroke = Color(0xFFE4E8F5)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "가족그룹 가입",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 그룹명 카드
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = cardFill),
                border = BorderStroke(1.dp, cardStroke),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }
            }

            // 그룹 설명 카드
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = 180.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = cardFill),
                border = BorderStroke(1.dp, cardStroke),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "그룹 설명",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B3240)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // ✅ 설명 텍스트 표시
                    Text(
                        description,
                        fontSize = 14.sp,
                        color = Color(0xFF111111)
                    )
                }
            }

            // 소유자 정보 카드
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = cardFill),
                border = BorderStroke(1.dp, cardStroke),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "소유자 정보",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B3240)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "소유자",
                            fontSize = 14.sp,
                            color = Color(0xFF9AA0AA),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            ownerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.joinGroup(
                                GroupJoinRequest(
                                    inviteCode = inviteCode,
                                    displayName = "내이름" // TODO: 사용자 이름 넣기
                                )
                            )
                            if (res.isSuccessful) {
                                Toast.makeText(ctx, "그룹에 가입되었습니다!", Toast.LENGTH_SHORT).show()
                                onJoined()
                            } else {
                                Toast.makeText(ctx, "가입 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) {
                Text("가입", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

/** ✅ 프리뷰는 테마를 감싸서 실제 화면과 최대한 동일하게 보이도록 */
@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GroupInfoScreenPreview() {
    MoagaTheme {
        GroupInfoScreen(inviteCode = "12345")
    }
}
