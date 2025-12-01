package com.moaga.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6

@Composable
fun ThisMonthReportDialog(
    onDismiss: () -> Unit,
    onNavigateToReport: (reportId: Int?, reportType: Int?, date: String?) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "이번 달 지출 현황",
                    fontSize = 20.sp,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "총 지출액",
                    fontSize = 14.sp,
                    fontFamily = font_paperlogy_6,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "3,566,000원",
                    fontSize = 24.sp,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAF4AEE),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        onDismiss() // 다이얼로그 닫기
                        onNavigateToReport(null, 1, "2025-09") // 이번 달 지출 현황 리포트로 이동
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAF4AEE)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "리포트 확인",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = font_paperlogy_6
                    )
                }
            }
        }
    }
}