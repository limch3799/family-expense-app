package com.moaga.app.ui.screens.plan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.data.api.dto.response.CurrentPlanResponse
import com.moaga.app.data.local.TokenManager
import com.moaga.app.data.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PlanUiState(
    val isLoading: Boolean = false,
    val currentPlan: CurrentPlanResponse? = null,
    val error: String? = null
)

data class FormattedTransaction(
    val time: String,
    val name: String,
    val amount: String
)

class PlanViewModel(
    private val planRepository: PlanRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    init {
        loadCurrentPlan()
    }

    private fun loadCurrentPlan() {
        val planIdInt = tokenManager.getPlanId() // Int
        if (planIdInt == -1) {
            _uiState.value = _uiState.value.copy(
                error = "플랜 ID를 찾을 수 없습니다.",
                isLoading = false,
                currentPlan = null
            )
            return
        }

        val planId = planIdInt.toLong() // Long 변환

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = planRepository.getCurrentPlan(planId) // Long 사용
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentPlan = response,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

    fun getFormattedTransactions(): List<FormattedTransaction> {
        return _uiState.value.currentPlan?.transactionsList?.map { transaction ->
            val formattedTime = formatTransactionDate(transaction.date)
            val formattedAmount = "+ ${formatAmount(transaction.amount.toLong())}원" // Long 변환
            FormattedTransaction(
                time = formattedTime,
                name = transaction.userName,
                amount = formattedAmount
            )
        } ?: emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedStartDate(): String {
        return _uiState.value.currentPlan?.createdAt?.let { createdAt ->
            try {
                val dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                "${dateTime.year}년 ${dateTime.monthValue}월 ${dateTime.dayOfMonth}일"
            } catch (e: Exception) {
                "날짜 정보 없음"
            }
        } ?: "날짜 정보 없음"
    }

    fun getProgress(): Float {
        val current = _uiState.value.currentPlan
        return if (current != null && current.targetAmount > 0) {
            current.amount.toFloat() / current.targetAmount.toFloat()
        } else {
            0f
        }
    }

    fun getFormattedCurrentAmount(): String {
        return _uiState.value.currentPlan?.amount?.let { formatAmount(it) } ?: "0"
    }

    fun getFormattedTargetAmount(): String {
        return _uiState.value.currentPlan?.targetAmount?.let { "${formatAmount(it)}원" } ?: "0원"
    }

    fun getDdayText(): String {
        return _uiState.value.currentPlan?.dday?.let { "D-$it" } ?: "D-0"
    }

    fun getPlanTitle(): String {
        return _uiState.value.currentPlan?.title ?: "플랜 정보 없음"
    }

    private fun formatTransactionDate(dateString: String): String {
        return try {
            if (dateString.length == 8) {
                val year = dateString.substring(0, 4)
                val month = dateString.substring(4, 6)
                val day = dateString.substring(6, 8)
                "$year.$month.$day 00:00:00"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatAmount(amount: Long): String {
        return String.format("%,d", amount)
    }

    fun refreshPlan() {
        loadCurrentPlan()
    }
}
