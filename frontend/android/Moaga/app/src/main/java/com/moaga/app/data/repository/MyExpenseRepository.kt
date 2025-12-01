package com.moaga.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.MyDailyTransactionsRequest
import com.moaga.app.data.api.dto.request.MyMonthlyCalendarRequest
import com.moaga.app.data.api.dto.response.MyDailyTransactionsResponse
import com.moaga.app.data.api.dto.response.MyMonthlyCalendarResponse
import com.moaga.app.data.api.dto.response.MySpendingTrendResponse
import com.moaga.app.data.utils.NetworkResult
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.average
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.maxOfOrNull

class MyExpenseRepository {
    private val apiService = ApiClient.apiService

    suspend fun getMonthlyCalendar(yearMonth: String): NetworkResult<MyMonthlyCalendarResponse> {
        return try {
            val response = apiService.getMyMonthlyCalendar(
                MyMonthlyCalendarRequest(yearMonth)
            )
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("월간 캘린더 데이터를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    suspend fun getDailyTransactions(date: String): NetworkResult<MyDailyTransactionsResponse> {
        return try {
            val response = apiService.getMyDailyTransactions(
                MyDailyTransactionsRequest(date)
            )
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("일별 거래내역을 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    suspend fun getSpendingTrend(): NetworkResult<MyExpenseChartData> {
        return try {
            val response = apiService.getMySpendingTrend()

            // 14일 전체 데이터로 차트용 처리
            val processedData = processMyExpenseChartData(response)
            NetworkResult.Success(processedData)
        } catch (e: HttpException) {
            NetworkResult.Error("지출 추이 데이터를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    // 14일 전체 데이터를 차트용으로 처리
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processMyExpenseChartData(response: MySpendingTrendResponse): MyExpenseChartData {
        val dailyAmounts = response.dailyAmounts

        // 0이 아닌 값들로만 평균 계산
        val nonZeroAmounts = dailyAmounts.filter { it.totalAmount > 0 }.map { it.totalAmount }
        val average = if (nonZeroAmounts.isNotEmpty()) {
            nonZeroAmounts.average().toInt()
        } else {
            50000 // 기본값
        }

        val today = LocalDate.now()

        // 14일 전체 데이터를 차트 아이템으로 변환
        val chartItems = dailyAmounts.map { dayData ->
            val date = LocalDate.parse(dayData.date)
            val isToday = date == today
            val displayDate = if (isToday) "오늘" else date.format(DateTimeFormatter.ofPattern("MM.dd"))

            MyExpenseChartItem(
                date = displayDate,
                amount = dayData.totalAmount,
                height = calculateBarHeight(dayData.totalAmount, average),
                localDate = date
            )
        }

        return MyExpenseChartData(
            chartItems = chartItems,
            maxAmount = dailyAmounts.maxOfOrNull { it.totalAmount } ?: 0
        )
    }

    private fun calculateBarHeight(amount: Int, average: Int): Int {
        return when {
            amount == 0 -> 5 // 최소 높이
            amount <= average / 2 -> 20 // 평균 절반 이하
            amount <= average -> 40 // 평균 이하 (중간점)
            amount <= average * 1.5 -> 60 // 평균 1.5배 이하
            else -> 70 // 평균 1.5배 초과
        }
    }
}

// 개인 지출용 차트 데이터 클래스들
data class MyExpenseChartData(
    val chartItems: List<MyExpenseChartItem>,
    val maxAmount: Int
)

data class MyExpenseChartItem(
    val date: String, // 화면에 표시될 날짜
    val amount: Int, // 실제 금액
    val height: Int, // 차트 막대 높이
    val localDate: LocalDate // 실제 날짜 객체
)