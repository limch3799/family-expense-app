package com.moaga.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.CurrentPlanRequest
import com.moaga.app.data.api.dto.request.SpendingTrendRequest
import com.moaga.app.data.api.dto.response.CurrentPlanResponse
import com.moaga.app.data.api.dto.response.LastUpdatedResponse
import com.moaga.app.data.api.dto.response.SpendingTrendResponse
import com.moaga.app.data.api.dto.response.TodayExpenseResponse
import com.moaga.app.data.utils.NetworkResult
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeRepository {
    private val apiService = ApiClient.apiService
    private val tokenManager = ApiClient.getTokenManager()

    suspend fun getTodayExpense(): NetworkResult<TodayExpenseResponse> {
        return try {
            val response = apiService.getTodayExpense()
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("오늘 지출 데이터를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
    suspend fun getCurrentPlan(planId: Long): NetworkResult<CurrentPlanResponse> {
        return try {
            val response = apiService.getCurrentPlan(CurrentPlanRequest(planId))
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("저축 플랜 데이터를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }


    suspend fun getLastUpdated(): NetworkResult<LastUpdatedResponse> {
        return try {
            val response = apiService.getLastUpdated()
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("업데이트 시간을 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSpendingTrend(): NetworkResult<SpendingTrendData> {
        return try {
            val groupId = tokenManager.getGroupId()
            if (groupId == -1) {
                return NetworkResult.Error("그룹 정보를 찾을 수 없습니다.")
            }

            val response = apiService.getSpendingTrend(SpendingTrendRequest(groupId))
            val processedData = processSpendingTrendData(response)

            NetworkResult.Success(processedData)
        } catch (e: HttpException) {
            NetworkResult.Error("지출 추이 데이터를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processSpendingTrendData(response: SpendingTrendResponse): SpendingTrendData {
        val dailyAmounts = response.dailyAmounts

        // 전체 평균값 계산 (0 제외)
        val nonZeroAmounts = dailyAmounts.filter { it.totalAmount > 0 }.map { it.totalAmount }
        val average = if (nonZeroAmounts.isNotEmpty()) {
            nonZeroAmounts.average().toInt()
        } else {
            50000 // 기본값
        }

        // 최근 7일 데이터 추출
        val recent7Days = dailyAmounts.takeLast(7)
        val today = LocalDate.now()

        val chartData = recent7Days.map { dayData ->
            val date = LocalDate.parse(dayData.date)
            val isToday = date == today
            val displayDate = if (isToday) "오늘" else date.format(DateTimeFormatter.ofPattern("MM.dd"))

            ChartDataPoint(
                date = displayDate,
                amount = dayData.totalAmount,
                height = calculateBarHeight(dayData.totalAmount, average),
                isToday = isToday
            )
        }

        val todayAmount = recent7Days.lastOrNull()?.totalAmount ?: 0

        return SpendingTrendData(
            todayAmount = todayAmount,
            chartData = chartData,
            maxAmount = dailyAmounts.maxOfOrNull { it.totalAmount } ?: 0
        )
    }

    private fun calculateBarHeight(amount: Int, average: Int): Int {
        return when {
            amount == 0 -> 5 // 최소 높이
            amount <= average / 2 -> 20 // 평균 절반 이하
            amount <= average -> 40 // 평균 이하
            amount <= average * 1.5 -> 60 // 평균 1.5배 이하
            else -> 70 // 평균 1.5배 초과
        }
    }
}

data class SpendingTrendData(
    val todayAmount: Int,
    val chartData: List<ChartDataPoint>,
    val maxAmount: Int
)

data class ChartDataPoint(
    val date: String,
    val amount: Int,
    val height: Int, // dp 단위
    val isToday: Boolean
)