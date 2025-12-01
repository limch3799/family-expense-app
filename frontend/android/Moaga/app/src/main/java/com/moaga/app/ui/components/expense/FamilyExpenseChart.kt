package com.moaga.app.ui.components.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6
import com.moaga.app.ui.theme.moaga_primary_medium
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun ExpenseChart(
    chartData: List<ChartItem>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(chartData.size - 1) }
    val scrollState = rememberScrollState()

    val maxAmount = chartData.maxOfOrNull { it.amount }?.takeIf { it > 0 } ?: 1
    val maxHeight = 80 // 막대 최대 높이(dp)

    // ✅ amount를 height로 변환 (비율 계산)
    val normalizedData = chartData.map {
        val normalizedHeight = (it.amount.toFloat() / maxAmount * maxHeight).toInt()
        it.copy(height = normalizedHeight)
    }

    // 처음 시작할 때 맨 오른쪽으로 스크롤
    LaunchedEffect(chartData) {
        if (chartData.isNotEmpty()) {
            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
            selectedIndex = chartData.size - 1
        }
    }

    Column(modifier = modifier) {

        if (chartData.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))

                normalizedData.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex

                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedIndex = index
                                item.localDate?.let { onDateSelected(it) }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 선택된 막대 위에 금액 표시
                        Box(
                            modifier = Modifier.height(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "-${item.formattedAmount}",
                                    fontSize = 8.sp,
                                    lineHeight = 8.sp,
                                    fontFamily = font_paperlogy_6,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFF10B981),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // 막대 그래프
                        Box(
                            modifier = Modifier
                                .height(maxHeight.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(item.height.dp)
                                    .background(
                                        brush = if (isSelected) {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF34D399), // emerald 400
                                                    Color(0xFF10B981)  // emerald 500
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFFD5D7DB), // gray 200
                                                    Color(0xFFB8BCC1)  // gray 300
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                                    .shadow(
                                        elevation = if (isSelected) 2.dp else 0.dp,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 날짜 표시
                        Text(
                            text = item.date,
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF6B7280),
                            fontFamily = moaga_primary_medium,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        } else {
            // 데이터 없을 때
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "차트 데이터를 불러오는 중...",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontFamily = font_gothic_5
                )
            }
        }
    }
}

data class ChartItem(
    val date: String,
    val height: Int,
    val amount: Int,
    val formattedAmount: String,
    val localDate: LocalDate? = null
)
