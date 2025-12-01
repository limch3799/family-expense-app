package com.moaga.app.ui.components.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moaga.app.R
import com.moaga.app.data.repository.ChartDataPoint
import com.moaga.app.ui.theme.*
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@Composable
fun FamilySpendingChangeCard(
    navController: NavController,
    todayAmount: String = "89,000원",
    chartData: List<ChartDataPoint> = emptyList(),
    onRefresh: (() -> Unit)? = null
) {
    var showLottie by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }

    val maxHeight = 80 // dp 기준 최대 막대 높이
    val maxAmount = chartData.maxOfOrNull { it.amount }?.takeIf { it > 0 } ?: 1

// ✅ 정규화된 데이터로 변환
    val normalizedData = chartData.map {
        val normalizedHeight = (it.amount.toFloat() / maxAmount * maxHeight).toInt()
        it.copy(height = normalizedHeight)
    }

    // 데이터가 비어있으면 1초간 Lottie 애니메이션 표시
    LaunchedEffect(chartData.isEmpty()) {
        if (chartData.isEmpty()) {
            showLottie = true
            startAnimation = false
            delay(1000) // 1초 대기
            showLottie = false
        } else {
            // 차트 데이터가 있으면 애니메이션 시작
            startAnimation = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(8.dp),
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 헤더 - 제목과 오늘 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "가족 지출 추이",
                    fontSize = 21.sp,
                    fontFamily = font_gothic_4,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )

                Text(
                    text = "오늘 $todayAmount",
                    fontSize = 10.sp,
                    fontFamily = moaga_primary_bold,
                    color = Color.White,
                    lineHeight = 12.sp,
                    modifier = Modifier
                        .background(/*
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF10B981), // 에메랄드 600
                                    Color(0xFF059669)  // 에메랄드 700
                                )
                            ),*/
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF3573F1),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Text(
                text = " 최근 7일간",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Start,
                fontFamily = font_gothic_4
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 차트 또는 Lottie 애니메이션 표시
            if (showLottie || chartData.isEmpty()) {
                // Lottie 애니메이션 표시
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_block))

                    LottieAnimation(
                        composition = composition,
                        iterations = 1, // 1번만 재생
                        modifier = Modifier.size(80.dp)
                    )
                }
            } else {
                // 차트 데이터 표시
                // 막대 그래프
                // 그래프 Row
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    normalizedData.forEachIndexed { index, dataPoint ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val animatedHeight by animateFloatAsState(
                                targetValue = if (startAnimation) dataPoint.height.toFloat() else 0f,
                                animationSpec = tween(
                                    durationMillis = 800,
                                    delayMillis = index * 100,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "barHeight"
                            )

                            Box(
                                modifier = Modifier.height(maxHeight.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(animatedHeight.dp) // ✅ 정규화된 높이
                                        .background(
                                            brush = if (dataPoint.isToday) {
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF34D399), Color(0xFF10B981))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFFD5D7DB), Color(0xFFB8BCC1))
                                                )
                                            },
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

// 날짜 Row
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    normalizedData.forEach { dataPoint ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dataPoint.date,
                                fontSize = 10.sp,
                                color = if (dataPoint.isToday) Color(0xFF10B981) else Color(0xFF6B7280),
                                fontFamily = moaga_primary_medium,
                                textAlign = TextAlign.Center,
                                fontWeight = if (dataPoint.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        //color = MaterialTheme.colorScheme.primary,
                        color = Color(0xFF22BD90),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { navController.navigate("expense_screen") }
                    .padding(vertical = 8.dp) // 세로 여백 조금 주기
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center, // 아이콘 + 텍스트 + 화살표 전체 가운데 정렬
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "차트",
                        tint = Color.White, // 흰색 아이콘
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "지출현황 상세보기",
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = Color.White, // 흰색 텍스트
                        fontFamily = font_gothic_4,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "화살표",
                        tint = Color.White, // 흰색 아이콘
                        modifier = Modifier.size(21.dp)
                    )
                }
            }


        }
    }
}