package com.moaga.app.ui.screens.analysis.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

data class CategoryAnalysisRequest(
    val groupId: Int,
    val yearMonth: String
)

data class CategoryExpense(
    val categoryId: Int,
    val categoryName: String,
    val amount: Long,
    val percentage: Double,
    val transactionCount: Int
)

data class CategoryAnalysisResponse(
    val yearMonth: String,
    val totalAmount: Long,
    val categoryExpenses: List<CategoryExpense>
)

data class ChartData(
    val categoryName: String,
    val amount: Long,
    val percentage: Double,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoryExpenseCard(
    year: Int?,
    month: Int?,
    onCategoryClick: (String, Long) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var categoryInfo by remember { mutableStateOf<CategoryAnalysisResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<ChartData?>(null) }


    val pastelColors = listOf(
        Color(0xFFFF8A95),
        Color(0xFFFFB366),
        Color(0xFF66B3FF),
        Color(0xFF8AFF66),
        Color(0xFFB366FF),
        Color(0xFFFFCC66),
        Color(0xFF8A66FF),
        Color(0xFFFF66B3),
        Color(0xFF66FFB3),
        Color(0xFFB3FF66)
    )


    LaunchedEffect(year, month) {
        if (year != null && month != null) {
            try {
                val groupId = tokenManager.getGroupId()
                val accessToken = tokenManager.getAccessToken()

                if (groupId != -1 && accessToken != null) {
                    val yearMonth = String.format("%04d-%02d", year, month)
                    val response = fetchCategoryAnalysis(
                        groupId = groupId,
                        yearMonth = yearMonth,
                        accessToken = accessToken
                    )
                    categoryInfo = response
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
                text = "카테고리별 지출",
                fontSize = 18.sp,
                fontFamily = font_gothic_5,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )


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

                categoryInfo != null -> {
                    val processedData = remember(categoryInfo) {
                        processChartData(categoryInfo!!.categoryExpenses, pastelColors)
                    }

                    // 차트와 말풍선을 포함하는 Box
                    Box(
                        modifier = Modifier
                            .height(320.dp) // 높이 증가
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        // 도넛 차트
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .align(Alignment.Center)
                        ) {
                            AnimatedDonutChart(
                                chartData = processedData,
                                modifier = Modifier.fillMaxSize(),
                                onSectionClick = { clickedData ->
                                    selectedCategory = if (selectedCategory == clickedData) null else clickedData
                                    onCategoryClick(clickedData.categoryName, clickedData.amount)
                                }
                            )
                        }

                        // 중앙 말풍선 - 차트 중앙에 위치
                        selectedCategory?.let { category ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(108.dp) // 크기 증가
                                    .clip(CircleShape)
                                    .background(category.color)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedCategory = null }
                                    .zIndex(10f), // 차트 위에 표시
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = category.categoryName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = font_gothic_5,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${formatAmount(category.amount)}원",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontFamily = font_gothic_5,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format("%.1f", category.percentage)}%",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontFamily = font_gothic_5,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 카테고리 리스트
                    processedData.forEach { data ->
                        CategoryItem(
                            categoryName = data.categoryName,
                            amount = data.amount,
                            percentage = data.percentage,
                            color = data.color,
                            onClick = { onCategoryClick(data.categoryName, data.amount) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun AnimatedDonutChart(
    chartData: List<ChartData>,
    modifier: Modifier = Modifier,
    onSectionClick: (ChartData) -> Unit
) {
    var currentAnimatingIndex by remember { mutableStateOf(0) }
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(chartData) {
        currentAnimatingIndex = 0
        animationProgress = 0f

        // 각 섹션을 순차적으로 애니메이션
        chartData.forEachIndexed { index, _ ->
            currentAnimatingIndex = index
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 200, // 각 섹션당 200ms
                    easing = EaseOutCubic
                )
            ) { value, _ ->
                animationProgress = value
            }
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(chartData) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val canvasSize = minOf(size.width, size.height)
                    val outerRadius = canvasSize / 2 - 30.dp.toPx()
                    val innerRadius = outerRadius * 0.6f

                    // 클릭 지점까지의 거리 계산
                    val distance = sqrt(
                        (offset.x - center.x).pow(2) + (offset.y - center.y).pow(2)
                    )

                    // 도넛 영역 내부인지 확인
                    if (distance in innerRadius..outerRadius) {
                        // 클릭한 각도 계산 (12시 방향 기준)
                        val angle = (atan2(offset.y - center.y, offset.x - center.x) * 180 / PI + 90).toFloat()
                        val normalizedAngle = if (angle < 0) angle + 360 else angle

                        // 해당하는 섹션 찾기
                        chartData.forEach { data ->
                            val startAngle = data.startAngle
                            val endAngle = data.startAngle + data.sweepAngle

                            if (normalizedAngle >= startAngle && normalizedAngle <= endAngle) {
                                onSectionClick(data)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        val canvasSize = minOf(size.width, size.height)
        val outerRadius = canvasSize / 2 - 30.dp.toPx()
        val innerRadius = outerRadius * 0.6f // 가운데 도넛 원
        val center = Offset(size.width / 2, size.height / 2)

        chartData.forEachIndexed { index, data ->
            val shouldAnimate = index <= currentAnimatingIndex
            val animatedSweepAngle = if (shouldAnimate) {
                if (index == currentAnimatingIndex) {
                    data.sweepAngle * animationProgress
                } else {
                    data.sweepAngle // 이미 완료된 섹션
                }
            } else {
                0f // 아직 시작하지 않은 섹션
            }

            // 도넛 차트 섹션 그리기
            if (animatedSweepAngle > 0) {
                // 외부 호
                drawArc(
                    color = data.color,
                    startAngle = data.startAngle - 90f, // 12시부터 시작
                    sweepAngle = animatedSweepAngle,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - outerRadius,
                        center.y - outerRadius
                    ),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )

                // 내부 흰색 원
                drawCircle(
                    color = Color.White,
                    radius = innerRadius,
                    center = center
                )

                // 구분선 그리기
                val startAngleRad = Math.toRadians((data.startAngle - 90f).toDouble())
                val endAngleRad = Math.toRadians((data.startAngle + animatedSweepAngle - 90f).toDouble())

                // 시작 구분선
                drawLine(
                    color = Color.White,
                    start = Offset(
                        center.x + innerRadius * cos(startAngleRad).toFloat(),
                        center.y + innerRadius * sin(startAngleRad).toFloat()
                    ),
                    end = Offset(
                        center.x + outerRadius * cos(startAngleRad).toFloat(),
                        center.y + outerRadius * sin(startAngleRad).toFloat()
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 끝 구분선
                if (animatedSweepAngle >= data.sweepAngle - 0.1f) {
                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x + innerRadius * cos(endAngleRad).toFloat(),
                            center.y + innerRadius * sin(endAngleRad).toFloat()
                        ),
                        end = Offset(
                            center.x + outerRadius * cos(endAngleRad).toFloat(),
                            center.y + outerRadius * sin(endAngleRad).toFloat()
                        ),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    categoryName: String,
    amount: Long,
    percentage: Double,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }

            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 색상 표시 원
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 퍼센트
        Text(
            text = "${String.format("%.1f", percentage)}%",
            fontSize = 12.sp,
            fontFamily = font_gothic_5,
            color = Color(0xFF999999),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 카테고리명
        Text(
            text = categoryName,
            fontSize = 14.sp,
            fontFamily = font_gothic_5,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )

        // 금액
        Text(
            text = "${formatAmount(amount)}원",
            fontSize = 14.sp,
            fontFamily = font_gothic_5,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

private fun processChartData(
    categoryExpenses: List<CategoryExpense>,
    colors: List<Color>
): List<ChartData> {
    // 1만원 이하 카테고리를 기타로 묶기
    val (mainCategories, otherCategories) = categoryExpenses.partition { it.amount >= 10000 }

    val processedCategories = mutableListOf<CategoryExpense>()
    processedCategories.addAll(mainCategories)

    // 기타 카테고리 생성
    if (otherCategories.isNotEmpty()) {
        val otherTotalAmount = otherCategories.sumOf { it.amount }
        val otherTotalPercentage = otherCategories.sumOf { it.percentage }
        val otherTotalCount = otherCategories.sumOf { it.transactionCount }

        processedCategories.add(
            CategoryExpense(
                categoryId = -1,
                categoryName = "기타",
                amount = otherTotalAmount,
                percentage = otherTotalPercentage,
                transactionCount = otherTotalCount
            )
        )
    }

    // 퍼센트 기준으로 정렬 (내림차순)
    val sortedCategories = processedCategories.sortedByDescending { it.percentage }

    var currentAngle = 0f
    return sortedCategories.mapIndexed { index, category ->
        val sweepAngle = (category.percentage / 100.0 * 360.0).toFloat()
        val color = colors[index % colors.size]

        val chartData = ChartData(
            categoryName = category.categoryName,
            amount = category.amount,
            percentage = category.percentage,
            color = color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle
        )

        currentAngle += sweepAngle
        chartData
    }
}

private fun formatAmount(amount: Long): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    return numberFormat.format(amount)
}

private suspend fun fetchCategoryAnalysis(
    groupId: Int,
    yearMonth: String,
    accessToken: String
): CategoryAnalysisResponse = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = CategoryAnalysisRequest(
        groupId = groupId,
        yearMonth = yearMonth
    )

    val json = Gson().toJson(requestBody)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://j13d105.p.ssafy.io/api/v1/analysis/group/category-analysis")
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
    Gson().fromJson(responseBody, CategoryAnalysisResponse::class.java)
}