package com.moaga.app.ui.screens.analysis.report

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
import java.util.concurrent.TimeUnit

data class ReportDetailRequest(
    val id: Int
)

data class ReportDetailResponse(
    val aiReportId: Int,
    val yearMonth: String,
    val reportContent: String,
    val generatedAt: String
)

@Composable
fun ExpenseInsightCard(
    reportId: Int?
) {
    // reportId가 null이면 카드를 표시하지 않음
    if (reportId == null) return

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var reportDetail by remember { mutableStateOf<ReportDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // API 호출
    LaunchedEffect(reportId) {
        try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken != null) {
                val response = fetchReportDetail(reportId, accessToken)
                reportDetail = response
            } else {
                errorMessage = "로그인 정보가 없습니다."
            }
        } catch (e: Exception) {
            errorMessage = "데이터를 불러올 수 없습니다: ${e.message}"
        } finally {
            isLoading = false
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
                text = "지출 패턴 인사이트",
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
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF666666)
                    )
                }

                reportDetail != null -> {
                    val processedContent = processReportContent(reportDetail!!.reportContent)

                    Text(
                        text = processedContent,
                        fontSize = 12.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF333333),
                        lineHeight = 18.sp
                    )
                }

                else -> {
                    Text(
                        text = "리포트 데이터가 없습니다.",
                        fontSize = 12.sp,
                        fontFamily = font_gothic_5,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

private fun processReportContent(content: String): String {
    // 첫 번째 타이틀 제거 (🏠 그룹 가계부 종합 리포트)
    val withoutFirstTitle = content.replace(Regex("# 🏠 그룹 가계부 종합 리포트\\s*\n*"), "")

    // ## 제목들을 굵게 처리하기 위해 특수 문자로 변환
    val processedContent = withoutFirstTitle
        .replace(Regex("## (.+)"), "【$1】") // ##를 【】로 변환
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // **굵게** 제거
        .replace(Regex("\\*(.+?)\\*"), "$1") // *기울임* 제거
        .replace("---", "") // 구분선 제거
        .replace(Regex("\n{3,}"), "\n\n") // 연속된 줄바꿈을 최대 2개로 제한
        .trim()

    return processedContent
}

private suspend fun fetchReportDetail(
    reportId: Int,
    accessToken: String
): ReportDetailResponse = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = ReportDetailRequest(id = reportId)
    val json = Gson().toJson(requestBody)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://j13d105.p.ssafy.io/api/v1/reports/detail")
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
    Gson().fromJson(responseBody, ReportDetailResponse::class.java)
}