package com.moaga.app.ui.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FamilyDepositCard(
    savingAccountNo: String = "",
    amount: Int = 0,
    isAmountHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    // 계좌번호 포맷팅 (4자리마다 - 추가)
    val formattedAccountNo = if (savingAccountNo.isNotEmpty()) {
        savingAccountNo.chunked(4).joinToString("-")
    } else {
        "0000-0000-0000-0000"
    }

    // 금액 포맷팅
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val formattedAmount = if (isAmountHidden) {
        "********"
    } else {
        numberFormat.format(amount)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, Color(0xFFECECEC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 첫 번째 Row: 예금 제목 + 송금하기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽: 우리가족 예금 + 계좌번호
                Column {
                    Text(
                        text = "우리가족 예금",
                        fontSize = 14.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 14.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "계좌번호 ",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            fontFamily = font_paperlogy_5
                        )
                        Text(
                            text = formattedAccountNo,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            fontFamily = font_paperlogy_5
                        )
                    }
                }

                // 오른쪽: 송금하기 버튼
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF1BBF8F),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "송금하기",
                            fontSize = 11.sp,
                            fontFamily = font_gothic_5,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 두 번째 Row: 숨김버튼 + 금액 + 원
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (isAmountHidden) "표시" else "숨김",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = moaga_primary_medium,
                    lineHeight = 10.sp,
                    modifier = Modifier
                        .alignByBaseline()
                        .background(
                            color = Color(0xFFD9D9D9),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onToggleVisibility()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formattedAmount,
                    fontSize = 38.sp,
                    fontFamily = moaga_primary_bold,
                    color = Color.Black,
                    modifier = Modifier.alignByBaseline()
                )

                Text(
                    text = " 원",
                    fontSize = 24.sp,
                    color = Color(0xFFACACAC),
                    fontFamily = font_paperlogy_6,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}