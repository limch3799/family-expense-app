package com.moaga.app.ui.components.expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.screens.expense.ExpenseDayData
import com.moaga.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseCalendar(
    currentMonth: YearMonth,
    today: LocalDate,
    monthlyExpenseData: Map<Int, ExpenseDayData>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 요일 헤더 위에 디바이더 추가
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(Color(0xFFE0E0E0))
        )
        Spacer(modifier = Modifier.height(4.dp))

        // 요일 헤더
        val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekdays.forEach { weekday ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = weekday,
                        fontSize = 10.sp,
                        color = when (weekday) {
                            "일" -> Color(0xFFEF5C82)
                            "토" -> Color(0xFF4B6AE8)
                            else -> Color(0xFF3D3D3D)
                        },
                        fontFamily = font_gothic_4,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 캘린더 그리드
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일을 0으로
        val daysInMonth = currentMonth.lengthOfMonth()

        // 캘린더 주 단위로 생성
        var dayCounter = 1
        repeat(6) { week ->
            if (dayCounter <= daysInMonth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { dayOfWeek ->
                        val currentDay = if (week == 0 && dayOfWeek < firstDayOfWeek) {
                            null
                        } else if (dayCounter <= daysInMonth) {
                            dayCounter++
                        } else {
                            null
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentDay != null) {
                                val currentDate = currentMonth.atDay(currentDay)
                                val isToday = currentDate == today
                                val isSelected = currentDate == selectedDate
                                val isFutureDate = currentDate.isAfter(today)
                                val isClickable = !isFutureDate

                                // 날짜 버튼
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected && isClickable -> Color(0xFF18A87E)
                                                isToday && isClickable -> Color(0xFF9E9D9D)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .let {
                                            if (isClickable) {
                                                it.clickable { onDateSelected(currentDate) }
                                            } else {
                                                it
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentDay.toString(),
                                        fontSize = 12.sp,
                                        color = when {
                                            isFutureDate -> Color(0xFFCCCCCC) // 미래 날짜는 회색
                                            isSelected -> Color.White
                                            isToday -> Color.White
                                            else -> Color(0xFF666666)
                                        },
                                        fontFamily = font_gothic_4,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // 금액 표시 (미래 날짜는 표시하지 않음)
                                if (!isFutureDate) {
                                    val displayText = monthlyExpenseData[currentDay]?.let { dayData ->
                                        if (dayData.formattedAmount.isNotEmpty()) {
                                            dayData.formattedAmount
                                        } else {
                                            " " // 공백 문자
                                        }
                                    } ?: " " // 데이터가 없을 때도 공백 문자

                                    Text(
                                        text = displayText,
                                        fontSize = 8.sp,
                                        color = Color(0xFFEF5C82),
                                        fontFamily = font_paperlogy_6,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = (-0.1).sp
                                    )
                                } else {
                                    Text(
                                        text = " ", // 미래 날짜도 공백 문자로 통일
                                        fontSize = 8.sp,
                                        color = Color.Transparent, // 투명하게 해서 안 보이게
                                        fontFamily = font_paperlogy_6,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = (-0.1).sp
                                    )
                                }
                            } else {
                                // 빈 날짜 셀
                                Box(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = " ",
                                    fontSize = 6.sp,
                                    color = Color.Transparent,
                                    fontFamily = font_paperlogy_6,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = (-0.1).sp
                                )
                            }
                        }
                    }
                }

                // 각 주 아래에 디바이더 추가 (마지막 주가 아닌 경우에만)
                val isLastWeek = (week == 5) || (dayCounter > daysInMonth)
                if (!isLastWeek) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.8.dp)
                            .background(Color(0xFFE0E0E0))
                    )
                }
            }
        }
    }
}