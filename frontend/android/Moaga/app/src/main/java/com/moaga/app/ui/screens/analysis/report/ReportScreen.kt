package com.moaga.app.ui.screens.analysis.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportScreen(
    reportId: Int? = null,
    reportType: Int? = null,
    date: String? = null,
    onBackClick: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }

    // 3초 후 로딩 완료
    LaunchedEffect(Unit) {
        delay(3000)
        isLoading = false
    }

    if (isLoading) {
        ReportLoadingScreen()
    } else {
        ReportContent(
            reportId = reportId,
            reportType = reportType,
            date = date,
            onBackClick = onBackClick
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ReportContent(
    reportId: Int?,
    reportType: Int?,
    date: String?,
    onBackClick: () -> Unit
) {
    // date에서 year, month 추출
    val (year, month) = remember(date) {
        if (date != null && date.length >= 7) {
            try {
                val parts = date.split("-")
                if (parts.size >= 2) {
                    Pair(parts[0].toInt(), parts[1].toInt())
                } else {
                    Pair(null, null)
                }
            } catch (e: Exception) {
                Pair(null, null)
            }
        } else {
            Pair(null, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 상단 고정 영역 (뒤로가기 버튼과 제목)
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 8.dp, end=16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 뒤로가기 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 뒤로가기 버튼
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF1A1A1A)
                    )
                }

                // 중앙 제목
                Text(
                    text = "가족 지출 리포트",
                    fontSize = 21.sp,
                    lineHeight = 21.sp,
                    fontFamily = font_gothic_5,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier
                        .weight(1f), // 남은 공간 전부 차지
                    textAlign = TextAlign.Center
                )

                // 오른쪽 공간 확보 (뒤로가기 버튼 크기만큼)
                Spacer(modifier = Modifier.size(48.dp))
            }


        }

        // 스크롤 가능한 내용 영역
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {


            // 조회기간과 분석대상 멤버 카드
            item {
                ReportPeriodMembersCard(
                    year = year,
                    month = month,
                    reportType = reportType
                )
            }

            // 총 지출 요약 카드
            item {
                ExpenseSummaryCard(
                    year = year,
                    month = month
                )
            }

            // 카테고리별 지출 카드
            item {
                CategoryExpenseCard(
                    year = year,
                    month = month,
                    onCategoryClick = { categoryName, amount ->
                        // 클릭 이벤트 처리
                        println("클릭된 카테고리: $categoryName, 금액: $amount")
                    }
                )
            }


            // 일별 지출 추이 카드 (새로 추가)
            item {
                MonthlyChartCard(
                    year = year,
                    month = month,
                    reportType = reportType
                )
            }



            item {
                MemberCategoryExpenseCard(
                    year = year,
                    month = month
                )
            }

            item {
                ExpenseInsightCard(
                    reportId = reportId
                )
            }




        }
    }
}

@Composable
private fun ParameterCard(
    title: String,
    value: String,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontFamily = font_paperlogy_6,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}