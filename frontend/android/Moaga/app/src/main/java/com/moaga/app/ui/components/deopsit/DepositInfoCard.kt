package com.moaga.app.ui.components.deposit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_5
import com.moaga.app.ui.theme.moaga_primary_medium

@Composable
@Preview
fun DepositInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(1.dp, Color(0xFFECECEC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 예금종류
            InfoRow(
                label = "예금종류",
                value = "가족공동예금"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 개설일자
            InfoRow(
                label = "개설일자",
                value = "2021년 1월 1일"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 예금주
            InfoRow(
                label = "예금주",
                value = "임창현"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 현재잔액
            InfoRow(
                label = "현재잔액",
                value = "2,120,000원"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 계좌번호
            InfoRow(
                label = "계좌번호",
                value = "1234-2123-2341-11"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 기본이율
            InfoRow(
                label = "기본이율",
                value = "0.1%"
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF666666),
            fontFamily = font_paperlogy_5
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.Black,
            fontFamily = font_gothic_5,
            fontWeight = FontWeight.Medium
        )
    }
}