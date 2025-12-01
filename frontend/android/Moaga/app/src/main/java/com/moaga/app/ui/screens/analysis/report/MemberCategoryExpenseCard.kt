package com.moaga.app.ui.screens.analysis.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

data class MemberAnalysisRequest(
    val groupId: Int,
    val yearMonth: String
)

data class MemberCategoryBreakdown(
    val categoryId: Int,
    val categoryName: String,
    val amount: Long,
    val percentage: Double
)

data class MemberAnalysis(
    val userId: Int,
    val memberName: String,
    val totalAmount: Long,
    val categoryBreakdown: List<MemberCategoryBreakdown>
)

data class MemberAnalysisResponse(
    val yearMonth: String,
    val memberAnalysis: List<MemberAnalysis>
)

data class MemberChartData(
    val categoryName: String,
    val amount: Long,
    val percentage: Double,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemberCategoryExpenseCard(
    year: Int?,
    month: Int?
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var memberAnalysisInfo by remember { mutableStateOf<MemberAnalysisResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedMemberIndex by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf<MemberChartData?>(null) } // ✅ 중앙 표시용

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
                    val response = fetchMemberAnalysis(
                        groupId = groupId,
                        yearMonth = yearMonth,
                        accessToken = accessToken
                    )
                    memberAnalysisInfo = response
                    selectedMemberIndex = 0
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
                text = "멤버별 카테고리 지출",
                fontSize = 18.sp,
                fontFamily = font_gothic_5,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        Text("오류 발생", fontSize = 16.sp, fontFamily = font_gothic_5, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", fontSize = 14.sp, fontFamily = font_gothic_5, color = Color(0xFF666666))
                    }
                }

                memberAnalysisInfo != null && memberAnalysisInfo!!.memberAnalysis.isNotEmpty() -> {
                    // ✅ 멤버 선택 칩
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(memberAnalysisInfo!!.memberAnalysis) { member ->
                            val index = memberAnalysisInfo!!.memberAnalysis.indexOf(member)
                            val isSelected = selectedMemberIndex == index

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isSelected) Color(0xFF6366F1) else Color.White)
                                    .border(
                                        width = if (isSelected) 0.dp else 2.dp,
                                        color = if (isSelected) Color.Transparent else Color(0xFF6366F1),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        selectedMemberIndex = index
                                        selectedCategory = null // 멤버 바뀌면 초기화
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = member.memberName,
                                    fontSize = 14.sp,
                                    fontFamily = font_gothic_5,
                                    color = if (isSelected) Color.White else Color(0xFF6366F1),
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val selectedMember = memberAnalysisInfo!!.memberAnalysis[selectedMemberIndex]
                    val processedData = remember(selectedMember, pastelColors) {
                        processMemberChartData(selectedMember.categoryBreakdown, pastelColors)
                    }

                    if (processedData.isNotEmpty()) {
                        // ✅ 도넛 차트 + 중앙 말풍선
                        Box(
                            modifier = Modifier
                                .height(320.dp)
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .align(Alignment.Center)
                            ) {
                                AnimatedDonutChart(
                                    chartData = processedData,
                                    modifier = Modifier.fillMaxSize(),
                                    onSectionClick = { clicked ->
                                        selectedCategory = if (selectedCategory == clicked) null else clicked
                                    }
                                )
                            }

                            selectedCategory?.let { category ->
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(108.dp)
                                        .clip(CircleShape)
                                        .background(category.color)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { selectedCategory = null }
                                        .zIndex(10f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                        Spacer(modifier = Modifier.height(12.dp))

                        processedData.forEach { data ->
                            MemberCategoryItem(data.categoryName, data.amount, data.color)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text("해당 멤버의 지출 데이터가 없습니다.", fontSize = 14.sp, fontFamily = font_gothic_5, color = Color(0xFF666666))
                    }
                }

                else -> {
                    Text("데이터가 없습니다.", fontSize = 16.sp, fontFamily = font_gothic_5, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun AnimatedDonutChart(
    chartData: List<MemberChartData>,
    modifier: Modifier = Modifier,
    onSectionClick: (MemberChartData) -> Unit
) {
    var currentAnimatingIndex by remember { mutableStateOf(0) }
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(chartData) {
        currentAnimatingIndex = 0
        animationProgress = 0f
        chartData.forEachIndexed { index, _ ->
            currentAnimatingIndex = index
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = EaseOutCubic)
            ) { value, _ -> animationProgress = value }
        }
    }

    Canvas(
        modifier = modifier.pointerInput(chartData) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val canvasSize = minOf(size.width, size.height)
                val outerRadius = canvasSize / 2 - 30.dp.toPx()
                val innerRadius = outerRadius * 0.6f
                val distance = sqrt((offset.x - center.x).pow(2) + (offset.y - center.y).pow(2))
                if (distance in innerRadius..outerRadius) {
                    val angle = (atan2(offset.y - center.y, offset.x - center.x) * 180 / PI + 90).toFloat()
                    val normalized = if (angle < 0) angle + 360 else angle
                    chartData.forEach { data ->
                        val end = data.startAngle + data.sweepAngle
                        if (normalized in data.startAngle..end) {
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
        val innerRadius = outerRadius * 0.6f
        val center = Offset(size.width / 2, size.height / 2)

        chartData.forEachIndexed { index, data ->
            val shouldAnimate = index <= currentAnimatingIndex
            val animatedSweep = if (shouldAnimate) {
                if (index == currentAnimatingIndex) data.sweepAngle * animationProgress else data.sweepAngle
            } else 0f

            if (animatedSweep > 0) {
                drawArc(
                    color = data.color,
                    startAngle = data.startAngle - 90f,
                    sweepAngle = animatedSweep,
                    useCenter = true,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )
                drawCircle(color = Color.White, radius = innerRadius, center = center)
            }
        }
    }
}

@Composable
private fun MemberCategoryItem(categoryName: String, amount: Long, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Text(categoryName, fontSize = 14.sp, fontFamily = font_gothic_5, color = Color(0xFF333333), modifier = Modifier.weight(1f))
        Text("${formatAmount(amount)}원", fontSize = 14.sp, fontFamily = font_gothic_5, color = Color(0xFF666666), fontWeight = FontWeight.Medium)
    }
}

private fun processMemberChartData(categoryBreakdown: List<MemberCategoryBreakdown>, colors: List<Color>): List<MemberChartData> {
    if (categoryBreakdown.isEmpty()) return emptyList()
    val (main, others) = categoryBreakdown.partition { it.amount >= 10000 }
    val processed = main.toMutableList()
    if (others.isNotEmpty()) {
        processed.add(MemberCategoryBreakdown(-1, "기타", others.sumOf { it.amount }, others.sumOf { it.percentage }))
    }
    val sorted = processed.sortedByDescending { it.percentage }
    var angle = 0f
    return sorted.mapIndexed { i, c ->
        val sweep = (c.percentage / 100 * 360).toFloat()
        val data = MemberChartData(c.categoryName, c.amount, c.percentage, colors[i % colors.size], angle, sweep)
        angle += sweep
        data
    }
}

private fun formatAmount(amount: Long): String = NumberFormat.getNumberInstance(Locale.KOREA).format(amount)

private suspend fun fetchMemberAnalysis(groupId: Int, yearMonth: String, accessToken: String): MemberAnalysisResponse =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        val body = Gson().toJson(MemberAnalysisRequest(groupId, yearMonth)).toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://j13d105.p.ssafy.io/api/v1/analysis/group/member-analysis")
            .post(body)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .addHeader("accept", "*/*")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}: ${response.message}")
        Gson().fromJson(response.body?.string(), MemberAnalysisResponse::class.java)
    }
