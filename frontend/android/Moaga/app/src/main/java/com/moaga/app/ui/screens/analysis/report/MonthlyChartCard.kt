package com.moaga.app.ui.screens.analysis.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.moaga.app.data.local.TokenManager
import com.moaga.app.ui.theme.font_gothic_5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.foundation.gestures.detectTapGestures

data class MonthlyCalendarRequest(
    val groupId: Int,
    val yearMonth: String
)

data class DailyExpense(
    val date: String,
    val totalAmount: Long,
    val transactionCount: Int
)

data class MonthlyCalendarResponse(
    val monthlyCalendar: List<DailyExpense>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyChartCard(
    year: Int?,
    month: Int?,
    reportType: Int?
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var calendarData by remember { mutableStateOf<MonthlyCalendarResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(year, month) {
        if (year != null && month != null) {
            try {
                val groupId = tokenManager.getGroupId()
                val accessToken = tokenManager.getAccessToken()

                if (groupId != -1 && accessToken != null) {
                    val yearMonth = String.format("%04d-%02d", year, month)
                    val response = fetchMonthlyCalendar(
                        groupId = groupId,
                        yearMonth = yearMonth,
                        accessToken = accessToken
                    )
                    calendarData = response
                } else {
                    errorMessage = "로그인 정보가 없습니다."
                }
            } catch (e: Exception) {
                errorMessage = "데이터를 불러올 수 없습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            errorMessage = "연도와 월 정보가 필요합니다."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "일별 지출 추이",
                fontSize = 18.sp,
                fontFamily = font_gothic_5,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6366F1),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                errorMessage != null -> {
                    Column {
                        Text(
                            text = "오류 발생",
                            fontSize = 16.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 14.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF666666)
                        )
                    }
                }

                calendarData != null -> {
                    val chartData = remember(calendarData, reportType) {
                        processChartData(calendarData!!.monthlyCalendar, reportType)
                    }

                    if (chartData.isNotEmpty()) {
                        DailyExpenseChart(
                            data = chartData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "표시할 데이터가 없습니다.",
                            fontSize = 14.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF666666)
                        )
                    }
                }

                else -> {
                    Text(
                        text = "데이터가 없습니다.",
                        fontSize = 16.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

data class ChartPoint(
    val day: Int,
    val amount: Long,
    val displayDate: String,
    val normalizedY: Float
)

@RequiresApi(Build.VERSION_CODES.O)
private fun processChartData(
    dailyExpenses: List<DailyExpense>,
    reportType: Int?
): List<ChartPoint> {
    if (dailyExpenses.isEmpty()) return emptyList()

    // reportType에 따라 필터링
    val filteredData = when (reportType) {
        1 -> {
            // 오늘까지만
            val today = LocalDate.now()
            dailyExpenses.filter { expense ->
                val expenseDate = LocalDate.parse(expense.date)
                !expenseDate.isAfter(today)
            }
        }
        else -> {
            // 전체 월
            dailyExpenses
        }
    }

    if (filteredData.isEmpty()) return emptyList()

    // 최대값, 최소값 계산
    val amounts = filteredData.map { it.totalAmount }
    val maxAmount = amounts.maxOrNull() ?: 0L
    val minAmount = amounts.minOrNull() ?: 0L

    return filteredData.map { expense ->
        val date = LocalDate.parse(expense.date)
        val day = date.dayOfMonth
        val displayDate = "${date.monthValue}/${day}"

        // Y 좌표 정규화 (0.0 ~ 1.0)
        val normalizedY = if (maxAmount > minAmount) {
            1f - (expense.totalAmount - minAmount) / (maxAmount - minAmount).toFloat()
        } else {
            0.5f // 모든 값이 같은 경우 중간에 배치
        }

        ChartPoint(
            day = day,
            amount = expense.totalAmount,
            displayDate = displayDate,
            normalizedY = normalizedY
        )
    }
}

@Composable
private fun DailyExpenseChart(
    data: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val pointSpacing = 40.dp // 컬럼 간격을 넓힘
    val chartWidth = with(density) { (data.size * pointSpacing.toPx()).toDp() }

    Box(modifier = modifier) {
        // 차트와 툴팁을 같은 Box 안에서 관리
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(chartWidth)
                    .height(240.dp) // 툴팁 공간 포함
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val stepX = pointSpacing.toPx()

                            // 어느 컬럼을 터치했는지 찾기
                            data.forEachIndexed { index, _ ->
                                val columnStart = index * stepX
                                val columnEnd = (index + 1) * stepX

                                if (offset.x >= columnStart && offset.x < columnEnd) {
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                    return@detectTapGestures
                                }
                            }
                            selectedPointIndex = null
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val topPadding = 60.dp.toPx() // 툴팁 공간
                val bottomPadding = 40.dp.toPx()
                val chartHeight = canvasHeight - topPadding - bottomPadding
                val stepX = pointSpacing.toPx()

                if (data.isNotEmpty()) {
                    // 점선 그래프 그리기
                    if (data.size > 1) {
                        val path = Path()
                        data.forEachIndexed { index, point ->
                            val x = (index + 0.5f) * stepX // 컬럼 중앙
                            val y = topPadding + point.normalizedY * chartHeight

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        // 점선 그리기
                        drawPath(
                            path = path,
                            color = Color(0xFF6366F1),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                            )
                        )
                    }

                    // 점, 날짜 레이블, 툴팁 그리기
                    data.forEachIndexed { index, point ->
                        val x = (index + 0.5f) * stepX // 컬럼 중앙
                        val y = topPadding + point.normalizedY * chartHeight

                        // 선택된 점은 더 크게 표시
                        val isSelected = selectedPointIndex == index
                        val pointRadius = if (isSelected) 5.dp.toPx() else 3.dp.toPx()
                        val pointColor = if (isSelected) Color(0xFF4F46E5) else Color(0xFF6366F1)

                        // 점 그리기
                        drawCircle(
                            color = pointColor,
                            radius = pointRadius,
                            center = Offset(x, y)
                        )

                        // 선택된 점의 툴팁 그리기
                        if (isSelected) {
                            val formattedAmount = NumberFormat.getInstance(Locale.KOREA).format(point.amount)
                            val tooltipY = 20.dp.toPx()

                            // 툴팁 배경
                            val tooltipWidth = 80.dp.toPx()
                            val tooltipHeight = 40.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFF333333),
                                topLeft = Offset(x - tooltipWidth/2, tooltipY),
                                size = Size(tooltipWidth, tooltipHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                            )

                            // 툴팁 텍스트
                            drawContext.canvas.nativeCanvas.apply {
                                // 날짜
                                drawText(
                                    point.displayDate,
                                    x,
                                    tooltipY + 15.dp.toPx(),
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 24f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                                // 금액
                                drawText(
                                    "${formattedAmount}원",
                                    x,
                                    tooltipY + 32.dp.toPx(),
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 26f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                )
                            }
                        }

                        // X축 날짜 레이벨
                        val labelY = canvasHeight - 15.dp.toPx()
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                point.displayDate,
                                x,
                                labelY,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#666666")
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



private suspend fun fetchMonthlyCalendar(
    groupId: Int,
    yearMonth: String,
    accessToken: String
): MonthlyCalendarResponse = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = MonthlyCalendarRequest(
        groupId = groupId,
        yearMonth = yearMonth
    )

    val json = Gson().toJson(requestBody)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://j13d105.p.ssafy.io/api/v1/analysis/group/monthly-calendar")
        .post(body)
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("Content-Type", "application/json")
        .addHeader("accept", "*/*")
        .build()

    val response = client.newCall(request).execute()

    if (!response.isSuccessful) {
        throw Exception("HTTP ${response.code}: ${response.message}")
    }

    val responseBody = response.body?.string() ?: throw Exception("Empty response")
    Gson().fromJson(responseBody, MonthlyCalendarResponse::class.java)
}

// 터치 감지를 위한 확장 함수
