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
import java.util.concurrent.TimeUnit

data class ReportMembersRequest(
    val year: Int,
    val month: Int,
    val groupId: Int
)

data class ReportMembersResponse(
    val startDay: String,
    val endDay: String,
    val names: List<String>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportPeriodMembersCard(
    year: Int?,
    month: Int?,
    reportType: Int?
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var periodInfo by remember { mutableStateOf<ReportMembersResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // API 호출
    LaunchedEffect(year, month) {
        if (year != null && month != null) {
            try {
                val groupId = tokenManager.getGroupId()
                val accessToken = tokenManager.getAccessToken()

                if (groupId != -1 && accessToken != null) {
                    val response = fetchReportMembers(
                        year = year,
                        month = month,
                        groupId = groupId,
                        accessToken = accessToken
                    )
                    periodInfo = response
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
                            .height(100.dp),
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

                periodInfo != null -> {
                    // 조회기간 섹션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "조회기간",
                            fontSize = 16.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF333333)
                        )

                        val displayPeriod = when (reportType) {
                            1 -> {
                                // 현재 월 1일 ~ 현재일
                                val today = java.time.LocalDate.now()
                                val startOfMonth = today.withDayOfMonth(1)
                                "${startOfMonth.toString().replace("-", ".")} ~ ${today.toString().replace("-", ".")}"
                            }
                            2 -> {
                                // 받은 연월 1일 ~ 말일
                                if (year != null && month != null) {
                                    val startDay = String.format("%04d.%02d.01", year, month)
                                    val lastDay = when (month) {
                                        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                                        4, 6, 9, 11 -> 30
                                        else -> 31
                                    }
                                    val endDay = String.format("%04d.%02d.%02d", year, month, lastDay)
                                    "$startDay ~ $endDay"
                                } else {
                                    "날짜 정보 없음"
                                }
                            }
                            else -> {
                                // 기본값: API 응답 사용
                                "${periodInfo!!.startDay.replace("-", ".")} ~ ${periodInfo!!.endDay.replace("-", ".")}"
                            }
                        }

                        Text(
                            text = displayPeriod,
                            fontSize = 16.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 분석대상 멤버 섹션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "분석대상 멤버",
                            fontSize = 16.sp,
                            fontFamily = font_gothic_5,
                            color = Color(0xFF333333)
                        )

                        Text(
                            text = periodInfo!!.names.joinToString(", "),
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

private suspend fun fetchReportMembers(
    year: Int,
    month: Int,
    groupId: Int,
    accessToken: String
): ReportMembersResponse = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = ReportMembersRequest(
        year = year,
        month = month,
        groupId = groupId
    )

    val json = Gson().toJson(requestBody)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://j13d105.p.ssafy.io/api/v1/reports/members")
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
    Gson().fromJson(responseBody, ReportMembersResponse::class.java)
}