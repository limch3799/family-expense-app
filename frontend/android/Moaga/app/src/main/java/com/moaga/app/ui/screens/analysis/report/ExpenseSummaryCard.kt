package com.moaga.app.ui.screens.analysis.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.util.*
import java.util.concurrent.TimeUnit

data class TotalExpenseRequest(
    val groupId: Int,
    val yearMonth: String
)

data class PeriodData(
    val yearMonth: String,
    val totalAmount: Long,
    val transactionCount: Int? = null
)

data class TotalExpenseResponse(
    val currentPeriod: PeriodData,
    val previousPeriod: PeriodData,
    val changeRate: String,
    val changeAmount: Long,
    val changeType: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseSummaryCard(
    year: Int?,
    month: Int?
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var expenseInfo by remember { mutableStateOf<TotalExpenseResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // API 호출
    LaunchedEffect(year, month) {
        if (year != null && month != null) {
            try {
                val groupId = tokenManager.getGroupId()
                val accessToken = tokenManager.getAccessToken()

                if (groupId != -1 && accessToken != null) {
                    val yearMonth = String.format("%04d-%02d", year, month)
                    val response = fetchTotalExpense(
                        groupId = groupId,
                        yearMonth = yearMonth,
                        accessToken = accessToken
                    )
                    expenseInfo = response
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
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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

                expenseInfo != null -> {
                    // 총 지출 요약 제목
                    Text(
                        text = "총 지출 요약",
                        fontSize = 16.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 총 지출 액수
                    Text(
                        text = "${formatAmount(expenseInfo!!.currentPeriod.totalAmount)}원",
                        fontSize = 24.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 증감률 및 타입
                    val changeColor = if (expenseInfo!!.changeType == "INCREASE") {
                        Color(0xFFE53E3E) // 빨간색 (증가)
                    } else {
                        Color(0xFF3182CE) // 파란색 (감소)
                    }

                    val changeText = if (expenseInfo!!.changeType == "INCREASE") {
                        "증가"
                    } else {
                        "감소"
                    }

                    Text(
                        text = "지난 기간 대비 ${expenseInfo!!.changeRate} $changeText",
                        fontSize = 14.sp,
                        fontFamily = font_gothic_5,
                        color = changeColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 증감 액수
                    val usageText = if (expenseInfo!!.changeType == "INCREASE") {
                        "더 사용했습니다"
                    } else {
                        "적게 사용했습니다"
                    }

                    Text(
                        text = "${formatAmount(expenseInfo!!.changeAmount)}원 $usageText",
                        fontSize = 16.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Bold
                    )
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

private fun formatAmount(amount: Long): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    return numberFormat.format(amount)
}

private suspend fun fetchTotalExpense(
    groupId: Int,
    yearMonth: String,
    accessToken: String
): TotalExpenseResponse = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = TotalExpenseRequest(
        groupId = groupId,
        yearMonth = yearMonth
    )

    val json = Gson().toJson(requestBody)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://j13d105.p.ssafy.io/api/v1/analysis/group/total-expense")
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
    Gson().fromJson(responseBody, TotalExpenseResponse::class.java)
}