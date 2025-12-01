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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.data.api.dto.response.CurrentPlanResponse
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_5
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavingPlanCard(
    planData: CurrentPlanResponse? = null,
    isLoading: Boolean = false,
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, Color(0xFFECECEC)),
    ) {
        if (isLoading) {
            // 로딩 상태
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF1BBB8D),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else if (planData != null) {
            // 실제 데이터 표시
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // 제목과 D-Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "진행 중인 저축 플랜",
                        fontSize = 14.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 14.sp
                    )

                    if (planData.dday > 0) {
                        Text(
                            text = "D-${planData.dday}",
                            fontSize = 12.sp,
                            fontFamily = font_gothic_5,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1BBB8D),
                            lineHeight = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 진행률 계산 및 표시
                val progressPercentage = if (planData.targetAmount > 0) {
                    (planData.amount.toFloat() / planData.targetAmount.toFloat() * 100)
                } else {
                    0f
                }
                val progressRatio = if (planData.targetAmount > 0) {
                    (planData.amount.toFloat() / planData.targetAmount.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${String.format("%.1f", progressPercentage)}%",
                        fontSize = 14.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 프로그래스 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio)
                            .height(16.dp)
                            .background(
                                color = Color(0xFF1BBB8D),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 현재금액/목표금액 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    Text(
                        text = "${formatter.format(planData.amount)}원 / ${formatter.format(planData.targetAmount)}원",
                        fontSize = 8.sp,
                        color = Color.Gray,
                        fontFamily = font_paperlogy_5,
                        lineHeight = 8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 목표명 표시
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "목표:",
                        fontSize = 10.5.sp,
                        color = Color(0xFF1A1A1A),
                        fontFamily = font_paperlogy_5,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = planData.title,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A1A),
                        fontFamily = font_paperlogy_5,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // 데이터가 없을 때 (플랜이 없는 경우)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "진행 중인 저축 플랜이 없습니다",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = font_gothic_5
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "새로운 플랜을 만들어보세요!",
                        fontSize = 12.sp,
                        color = Color(0xFF1BBB8D),
                        fontFamily = font_paperlogy_5
                    )
                }
            }
        }
    }
}