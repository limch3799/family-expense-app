// file: app/src/main/java/com/moaga/app/ui/screens/plan/DepositHistoryContent.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DepositHistoryContent(
    primaryGreen: Color,
    titleColor: Color,
    transactions: List<FormattedTransaction> = emptyList()
) {
    val captionColor = Color(0xFF6C7682)

    if (transactions.isEmpty()) {
        // 빈 상태 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "입금 내역이 없습니다.",
                fontSize = 14.sp,
                color = captionColor
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 140.dp) // 바텀 네비게이션을 위한 여백 추가
        ) {
            itemsIndexed(transactions) { idx, transaction ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(transaction.time, fontSize = 12.sp, color = Color(0xFF9AA2A9))
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(transaction.name, fontSize = 15.sp, color = titleColor)
                        Text(
                            transaction.amount,
                            fontSize = 15.sp,
                            color = primaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (idx != transactions.lastIndex) {
                    Divider(color = Color(0xFFE0E0E0))
                }
            }
        }
    }
}