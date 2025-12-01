package com.moaga.app.ui.screens.expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.response.MonthlyCalendarItem
import com.moaga.app.data.api.dto.response.TransactionItem
import com.moaga.app.data.repository.ExpenseRepository
import com.moaga.app.data.utils.NetworkResult
import com.moaga.app.ui.components.expense.ChartItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val expenseRepository = ExpenseRepository()
    private val apiService = ApiClient.apiService

    var currentUserName: String? = null
        private set

    init {
        loadInitialData()
        loadUserInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialData() {
        viewModelScope.launch {
            loadLastUpdated()
            loadMonthlyCalendar(YearMonth.now())
            loadSpendingTrend() // 차트 데이터 로드 추가
            // 오늘 날짜의 거래 내역을 초기에 자동으로 로드 (두 번째 파일에서 가져온 개선사항)
            loadDailyTransactions(LocalDate.now())
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val response = apiService.getUserInfo()
                currentUserName = response.username   // ✅ 로그인한 사용자 이름 저장
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadLastUpdated() {
        val result = expenseRepository.getLastUpdated()
        when (result) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    lastUpdated = result.data.lastUpdated, // ✅ 원본 그대로
                    error = null
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(error = result.message)
            }
            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadMonthlyCalendar(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val yearMonthString = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val result = expenseRepository.getMonthlyCalendar(yearMonthString)

        when (result) {
            is NetworkResult.Success -> {
                // 날짜별 지출 데이터를 Map으로 변환
                val expenseDataMap = result.data.monthlyCalendar.associate { item ->
                    val dayOfMonth = LocalDate.parse(item.date).dayOfMonth
                    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                    val expenseData = ExpenseDayData(
                        amount = item.totalAmount,
                        formattedAmount = if (item.totalAmount > 0) "-${numberFormat.format(item.totalAmount)}" else "",
                        transactionCount = item.transactionCount
                    )
                    dayOfMonth to expenseData
                }

                _uiState.value = _uiState.value.copy(
                    monthlyExpenseData = expenseDataMap,
                    isLoading = false,
                    error = null
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isLoading = false
                )
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    suspend fun loadSpendingTrend() {
        val result = expenseRepository.getSpendingTrend()
        when (result) {
            is NetworkResult.Success -> {
                // Repository에서 처리된 데이터를 ChartItem으로 변환
                val chartItems = result.data.chartItems.map { expenseChartItem ->
                    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                    ChartItem(
                        date = expenseChartItem.date,
                        height = expenseChartItem.height,
                        amount = expenseChartItem.amount,
                        formattedAmount = numberFormat.format(expenseChartItem.amount),
                        localDate = expenseChartItem.localDate
                    )
                }

                _uiState.value = _uiState.value.copy(
                    chartData = chartItems,
                    error = null
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    error = result.message
                )
            }
            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDailyTransactions(selectedDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val dateString = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val result = expenseRepository.getDailyTransactions(dateString)

            when (result) {
                is NetworkResult.Success -> {
                    // TransactionItem을 ExpenseItem으로 변환
                    val expenseItems = result.data.transactions.map { transaction ->
                        // 날짜와 시간 포맷팅
                        val date = selectedDate.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
                        val time = transaction.transactionTime.replace("Z", "").let { timeStr ->
                            try {
                                val parts = timeStr.split(":")
                                "${parts[0]}:${parts[1]}:${parts[2]}"
                            } catch (e: Exception) {
                                timeStr
                            }
                        }

                        // 금액 포맷팅
                        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                        val formattedAmount = "-${numberFormat.format(transaction.amount)}"

                        ExpenseItem(
                            id = transaction.transactionId,
                            date = date,
                            time = time,
                            person = transaction.realName,
                            category = transaction.categoryName,
                            name = transaction.description,
                            amount = formattedAmount,
                            isExcluded = transaction.isExcluded
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        expenseItems = expenseItems,
                        transactionCount = result.data.totalCount,
                        isLoading = false,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    // 연도/월 변경 시 지출내역 초기화 함수 추가 (두 번째 파일에서 가져온 개선사항)
    fun clearTransactionData() {
        _uiState.value = _uiState.value.copy(
            expenseItems = emptyList(),
            transactionCount = 0
        )
    }

    fun refreshLastUpdated(currentMonth: YearMonth) {
        viewModelScope.launch {
            loadLastUpdated()
            loadMonthlyCalendar(currentMonth) // ✅ 현재 보고 있는 달 다시 로드
            loadSpendingTrend() // 차트 데이터도 함께 새로고침
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

//    fun changeCategory(transactionId: Int, categoryId: Int, newCategoryName: String, onComplete: () -> Unit) {
//        viewModelScope.launch {
//            when (val result = expenseRepository.changeTransactionCategory(transactionId, categoryId)) {
//                is NetworkResult.Success -> {
//                    val currentItems = _uiState.value.expenseItems.toMutableList()
//
//                    val index = currentItems.indexOfFirst { it.id == transactionId }
//                    if (index != -1) {
//                        val updatedItem = currentItems[index].copy(category = newCategoryName)
//                        currentItems[index] = updatedItem
//
//                        _uiState.value = _uiState.value.copy(expenseItems = currentItems)
//                    }
//
//                    onComplete()
//                    refreshLastUpdated()
//                }
//                is NetworkResult.Error -> {
//                    _uiState.value = _uiState.value.copy(error = result.message)
//                }
//                else -> {}
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshMonthlyAndChartData(currentMonth: YearMonth) {
        viewModelScope.launch {
            // ✅ 기존 데이터 먼저 초기화 → Composable 강제 리렌더링
            _uiState.value = _uiState.value.copy(
                monthlyExpenseData = emptyMap(),
                chartData = emptyList()
            )

            // ✅ 새 데이터 로딩
            loadMonthlyCalendar(currentMonth)
            loadSpendingTrend()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeCategoryAndExclude(
        transactionId: Int,
        categoryId: Int,
        newCategoryName: String,
        exclude: Boolean,
        currentMonth: YearMonth,
        selectedDate: LocalDate?,
        onComplete: () -> Unit
    ) {
        println("🔥 changeCategoryAndExclude 시작: ID=$transactionId, exclude=$exclude")

        viewModelScope.launch {
            try {
                // 먼저 UI에서 즉시 업데이트 (낙관적 업데이트)
                updateUIImmediately(transactionId, newCategoryName, exclude)

                // 그 다음 서버 API 호출
                val categoryResult = expenseRepository.changeTransactionCategory(transactionId, categoryId)
                val excludeResult = if (exclude) {
                    expenseRepository.excludeTransaction(transactionId)
                } else {
                    expenseRepository.includeTransaction(transactionId)
                }

                println("🔥 API 결과 - 카테고리: $categoryResult, 제외: $excludeResult")

                // ✅ 카테고리 변경은 실패해도 제외 상태 변경이 성공하면 OK
                val isAnySuccess = categoryResult is NetworkResult.Success || excludeResult is NetworkResult.Success

                if (isAnySuccess) {
                    onComplete()

                    // ✅ 서버 데이터 재로드 시 현재 변경사항 보존
                    preserveUIChangesAndRefresh(transactionId, exclude, currentMonth, selectedDate)
                } else {
                    // 모든 API 실패 시 UI 롤백
                    rollbackUIUpdate(transactionId)

                    val errorMsg = (categoryResult as? NetworkResult.Error)?.message
                        ?: (excludeResult as? NetworkResult.Error)?.message
                        ?: "변경 실패"
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                }
            } catch (e: Exception) {
                println("🔥 changeCategoryAndExclude 오류: ${e.message}")
                rollbackUIUpdate(transactionId)
                _uiState.value = _uiState.value.copy(error = "변경 중 오류 발생: ${e.message}")
            }
        }
    }

    // ✅ 변경사항을 보존하면서 서버 데이터와 동기화하는 새로운 함수
    private fun preserveUIChangesAndRefresh(
        transactionId: Int,
        exclude: Boolean,
        currentMonth: YearMonth,
        selectedDate: LocalDate?
    ) {
        viewModelScope.launch {
            println("🔥 변경사항 보존하며 서버 데이터 동기화 시작")

            // 현재 변경된 아이템 정보 저장
            val currentItem = _uiState.value.expenseItems.find { it.id == transactionId }

            // ✅ 먼저 거래내역만 새로고침 (이게 가장 중요!)
            selectedDate?.let { date ->
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val result = expenseRepository.getDailyTransactions(dateString)

                when (result) {
                    is NetworkResult.Success -> {
                        val expenseItems = result.data.transactions.map { transaction ->
                            val itemDate = date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
                            val time = transaction.transactionTime.replace("Z", "").let { timeStr ->
                                try {
                                    val parts = timeStr.split(":")
                                    "${parts[0]}:${parts[1]}:${parts[2]}"
                                } catch (e: Exception) {
                                    timeStr
                                }
                            }

                            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                            val formattedAmount = "-${numberFormat.format(transaction.amount)}"

                            ExpenseItem(
                                id = transaction.transactionId,
                                date = itemDate,
                                time = time,
                                person = transaction.realName,
                                category = transaction.categoryName,
                                name = transaction.description,
                                amount = formattedAmount,
                                isExcluded = transaction.isExcluded // ✅ 서버에서 온 최신 제외 상태 사용
                            )
                        }

                        // ✅ 거래내역 즉시 업데이트
                        _uiState.value = _uiState.value.copy(
                            expenseItems = expenseItems,
                            transactionCount = result.data.totalCount,
                            isLoading = false,
                            error = null
                        )

                        println("🔥 거래내역 새로고침 완료 - 서버 상태 반영됨")

                        // ✅ 달력과 차트는 백그라운드에서 천천히 동기화 (UI 블로킹 없이)
                        launch {
                            delay(100) // 약간의 지연으로 UI 끊김 방지
                            println("🔥 달력/차트 백그라운드 동기화 시작")
                            loadMonthlyCalendar(currentMonth)
                            loadSpendingTrend()
                            println("🔥 달력/차트 백그라운드 동기화 완료")
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } ?: run {
                // selectedDate가 없는 경우에만 모든 데이터 새로고침
                launch {
                    loadMonthlyCalendar(currentMonth)
                    loadSpendingTrend()
                }
            }

            println("🔥 서버 데이터 동기화 완료")
        }
    }

    private fun updateUIImmediately(transactionId: Int, newCategoryName: String, exclude: Boolean) {
        println("🔥 즉시 UI 업데이트 시작: ID=$transactionId, exclude=$exclude")

        val currentState = _uiState.value
        val currentItems = currentState.expenseItems.toMutableList()
        val index = currentItems.indexOfFirst { it.id == transactionId }

        if (index != -1) {
            val oldItem = currentItems[index]
            val updatedItem = oldItem.copy(
                category = newCategoryName,
                isExcluded = exclude
            )
            currentItems[index] = updatedItem

            println("🔥 아이템 업데이트: ${oldItem.name} - 제외여부: ${oldItem.isExcluded} → $exclude")

            // UI State 즉시 업데이트
            _uiState.value = currentState.copy(
                expenseItems = currentItems
            )

            // 달력과 차트 데이터도 즉시 업데이트
            updateCalendarDataForExcludeChange(oldItem, exclude)
            updateChartDataForExcludeChange(oldItem, exclude)

            println("🔥 UI 업데이트 완료")
        } else {
            println("🔥 해당 ID의 아이템을 찾을 수 없음: $transactionId")
        }
    }

    // 4. UI 롤백을 위한 함수 (원본 데이터로 복구)
    private suspend fun rollbackUIUpdate(transactionId: Int) {
        println("🔥 UI 롤백 시작")
        // 서버에서 최신 데이터를 다시 가져와서 UI를 올바른 상태로 복구
        val currentState = _uiState.value
        // 여기서는 간단히 에러 상태만 설정하고, 사용자가 새로고침하도록 안내
        _uiState.value = currentState.copy(error = "변경에 실패했습니다. 새로고침 후 다시 시도해주세요.")
    }

// 5. ExpenseItemComponent가 isExcluded 상태를 올바르게 반영하는지 확인
// ExpenseItemComponent에서 isExcluded에 따른 스타일링이 적용되어야 함

    // 6. 추가 디버깅을 위한 StateFlow 관찰
    fun debugUIState() {
        viewModelScope.launch {
            uiState.collect { state ->
                println("🔥 UI State 변경: expenseItems 수 = ${state.expenseItems.size}")
                state.expenseItems.forEach { item ->
                    println("🔥 아이템: ${item.name}, 제외여부: ${item.isExcluded}")
                }
            }
        }
    }

    // ✅ 달력 데이터 즉시 업데이트를 위한 새로운 헬퍼 함수
    private fun updateCalendarDataForExcludeChange(item: ExpenseItem, exclude: Boolean) {
        try {
            val mutableMonthlyData = _uiState.value.monthlyExpenseData.toMutableMap()

            // 날짜 파싱 (yy.MM.dd 형식)
            val dateParts = item.date.split(".")
            if (dateParts.size == 3) {
                val year = 2000 + dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val day = dateParts[2].toInt()

                val existing = mutableMonthlyData[day]
                if (existing != null) {
                    // 금액에서 쉼표와 마이너스 제거 후 숫자로 변환
                    val itemAmount = item.amount
                        .replace("-", "")
                        .replace(",", "")
                        .toIntOrNull() ?: 0

                    val newAmount = if (exclude) {
                        // 제외 시 금액 차감
                        maxOf(0, existing.amount - itemAmount)
                    } else {
                        // 포함 시 금액 추가
                        existing.amount + itemAmount
                    }

                    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                    mutableMonthlyData[day] = existing.copy(
                        amount = newAmount,
                        formattedAmount = if (newAmount > 0) "-${numberFormat.format(newAmount)}" else ""
                    )

                    _uiState.value = _uiState.value.copy(
                        monthlyExpenseData = mutableMonthlyData
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시에는 서버 데이터로 다시 로드되도록 함
        }
    }

    // ✅ 차트 데이터 즉시 업데이트를 위한 새로운 헬퍼 함수
    private fun updateChartDataForExcludeChange(item: ExpenseItem, exclude: Boolean) {
        try {
            val currentChartData = _uiState.value.chartData.toMutableList()

            // 거래 날짜를 LocalDate로 변환
            val dateParts = item.date.split(".")
            if (dateParts.size == 3) {
                val year = 2000 + dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val day = dateParts[2].toInt()
                val transactionDate = LocalDate.of(year, month, day)

                // 해당 날짜의 차트 아이템 찾기
                val chartItemIndex = currentChartData.indexOfFirst {
                    it.localDate == transactionDate
                }

                if (chartItemIndex != -1) {
                    val existingChartItem = currentChartData[chartItemIndex]

                    // 금액 계산
                    val itemAmount = item.amount
                        .replace("-", "")
                        .replace(",", "")
                        .toIntOrNull() ?: 0

                    val newAmount = if (exclude) {
                        maxOf(0, existingChartItem.amount - itemAmount)
                    } else {
                        existingChartItem.amount + itemAmount
                    }

                    // 차트 높이 재계산 (전체 차트 데이터 기준으로)
                    val maxAmount = currentChartData.maxOfOrNull {
                        if (it.localDate == transactionDate) newAmount else it.amount
                    } ?: 1

                    val newHeight = if (newAmount > 0) {
                        ((newAmount.toFloat() / maxAmount.toFloat()) * 100).coerceAtLeast(8f)
                    } else {
                        0f
                    }

                    // 먼저 금액만 업데이트
                    currentChartData[chartItemIndex] = existingChartItem.copy(
                        amount = newAmount
                    )

                    // 업데이트된 데이터 기준으로 최대값 계산
                    val finalMaxAmount = currentChartData.maxOfOrNull { it.amount } ?: 1

                    // 모든 차트 아이템의 높이와 포맷된 금액을 새로운 최대값 기준으로 재계산
                    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                    val finalChartData = currentChartData.map { chartItem ->
                        val calculatedHeight = if (chartItem.amount > 0) {
                            ((chartItem.amount.toFloat() / finalMaxAmount.toFloat()) * 100).coerceAtLeast(8f)
                        } else {
                            0f
                        }

                        chartItem.copy(
                            height = calculatedHeight.toInt(),
                            formattedAmount = numberFormat.format(chartItem.amount.toInt())
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        chartData = finalChartData
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시에는 서버 데이터로 다시 로드되도록 함
        }
    }



}

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val lastUpdated: String = "",
    val monthlyExpenseData: Map<Int, ExpenseDayData> = emptyMap(),
    val expenseItems: List<ExpenseItem> = emptyList(),
    val transactionCount: Int = 0,
    val chartData: List<ChartItem> = emptyList(), // 차트 데이터 추가
    val error: String? = null
)

data class ExpenseDayData(
    val amount: Int,
    val formattedAmount: String,
    val transactionCount: Int
)

data class ExpenseItem(
    val id: Int,
    val date: String,
    val time: String,
    val person: String,
    val category: String,
    val name: String,
    val amount: String,
    val isExcluded: Boolean
)