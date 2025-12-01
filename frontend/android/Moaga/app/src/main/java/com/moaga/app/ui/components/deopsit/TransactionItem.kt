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
import com.moaga.app.ui.screens.deposit.Transaction
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_5
import com.moaga.app.ui.theme.moaga_primary_medium

@Composable
fun TransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(0.8.dp, Color(0xFFECECEC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 날짜와 시간
            Text(
                text = transaction.dateTime,
                fontSize = 11.sp,
                color = Color(0xFF999999),
                fontFamily = font_paperlogy_5,
                lineHeight = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 사용자명과 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.userName,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = transaction.amount,
                    fontSize = 14.sp,
                    color = if (transaction.amount.startsWith("+")) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFE53E3E)
                    },
                    fontFamily = moaga_primary_medium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun TransactionItemPreview() {
    TransactionItem(
        transaction = Transaction(
            dateTime = "2024.01.15 14:30:25",
            userName = "임창현",
            amount = "+120,000원"
        )
    )
}