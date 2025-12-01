// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanDetailScreen.kt
package com.moaga.app.ui.screens.plan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    onBack: () -> Unit = {},
    onClosed: () -> Unit = {},
    title: String = "저축 플랜",
    planName: String = "여름 휴가비 모으기",
    savedAmount: String = "755,500원",
    startDate: String = "2025년 9월 1일",
    endDate: String = "2025년 9월 30일",
    goalAmount: String = "1,000,000원",
    depositAccount: String = "123-1234-1234-324012"
) {
    val primaryGreen = Color(0xFF18A87E)
    val headerBg     = Color(0xFFEAF6F2)   // 상단 큰 카드 배경(연 민트)
    val titleColor   = Color(0xFF111111)
    val captionColor = Color(0xFF6C7682)
    val dangerPink   = Color(0xFFFFAFAF)   // 하단 해지 버튼 배경

    var showConfirm by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val ctx = LocalContext.current

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("플랜 종료(해지)") },
            text = { Text("정말 플랜을 종료하시겠어요? 저장된 저축 내역은 유지되며, 이후 자동 이체/목표 추적이 중단됩니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onClosed()
                }) { Text("종료", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("취소") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = titleColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // 화면 하단 해지 버튼 (핑크톤)
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = dangerPink)
                ) {
                    Text("플랜 종료(해지)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .verticalScroll(scroll)
        ) {
            // 상단 큰 카드
            Surface(
                color = headerBg,
                shape = RoundedCornerShape(0.dp),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)) {
                    Text(planName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
                    Spacer(Modifier.height(10.dp))
                    Surface(color = Color(0xFFDDEBE7), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "저축 금액",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = captionColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = savedAmount.dropLast(1), // "원" 제외 숫자만 굵게
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("원", fontSize = 18.sp, color = captionColor)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("플랜 정보", modifier = Modifier.padding(horizontal = 20.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(10.dp))

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                InfoRow("플랜 명", planName)
                InfoRow("시작 일자", startDate)
                InfoRow("종료 일자", endDate)
                InfoRow("목표 금액", goalAmount)
                InfoRow("저축 금액", savedAmount)

                // 입금 계좌번호 + 복사
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("입금 계좌번호", color = captionColor, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(depositAccount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE5E6EA),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("account", depositAccount))
                                Toast.makeText(ctx, "계좌번호가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("복사", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, color = Color(0xFF555A61))
                        }
                    }
                }
            }

            Spacer(Modifier.height(60.dp)) // 내용 끝 여백
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val titleColor = Color(0xFF111111)
    val caption    = Color(0xFF6C7682)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = caption, fontSize = 14.sp)
        Text(value, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PlanDetailPreview() {
    PlanDetailScreen()
}
