package com.moaga.app.ui.screens.expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.data.repository.MyExpenseRepository
import com.moaga.app.data.utils.NetworkResult
import com.moaga.app.ui.components.expense.ChartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class MyExpenseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyExpenseUiState())
    val uiState: StateFlow<MyExpenseUiState> = _uiState.asStateFlow()

    private val myExpenseRepository = MyExpenseRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadMonthlyCalendar(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val yearMonthString = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val result = myExpenseRepository.getMonthlyCalendar(yearMonthString)

        when (result) {
            is NetworkResult.Success -> {
                // 날짜별 지출 데이터를 Map으로 변환
                val expenseDataMap = result.data.monthlyCalendar.associate { item ->
                    val dayOfMonth = LocalDate.parse(item.date).dayOfMonth
                    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                    val expenseData = MyExpenseDayData(
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
        val result = myExpenseRepository.getSpendingTrend()
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
            val result = myExpenseRepository.getDailyTransactions(dateString)

            when (result) {
                is NetworkResult.Success -> {
                    // TransactionItem을 MyExpenseItem으로 변환
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

                        MyExpenseItem(
                            date = date,
                            time = time,
                            category = transaction.categoryName,
                            name = transaction.description,
                            amount = formattedAmount
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        expenseItems = expenseItems,
                        transactionCount = result.data.transactionCount,
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
}

data class MyExpenseUiState(
    val isLoading: Boolean = false,
    val monthlyExpenseData: Map<Int, MyExpenseDayData> = emptyMap(),
    val expenseItems: List<MyExpenseItem> = emptyList(),
    val transactionCount: Int = 0,
    val chartData: List<ChartItem> = emptyList(),
    val error: String? = null
)

data class MyExpenseDayData(
    val amount: Int,
    val formattedAmount: String,
    val transactionCount: Int
)

data class MyExpenseItem(
    val date: String,
    val time: String,
    val category: String,
    val name: String,
    val amount: String
)