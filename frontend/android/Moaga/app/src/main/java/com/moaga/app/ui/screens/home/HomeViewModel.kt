package com.moaga.app.ui.screens.home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.data.api.dto.response.CurrentPlanResponse
import com.moaga.app.data.local.TokenManager
import com.moaga.app.data.repository.HomeRepository
import com.moaga.app.data.repository.SpendingTrendData
import com.moaga.app.data.utils.NetworkResult
import com.moaga.app.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val context: Context,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val tokenManager = TokenManager(context)
    private val homeRepository = HomeRepository()

    init {
        loadHomeData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                loadTodayExpenseData()
                loadSpendingTrendData()
                loadFamilyDepositData()
                loadAccountData()
                loadCurrentPlanData() // 플랜 데이터 로드 추가
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "데이터를 불러오는 중 오류가 발생했습니다.",
                    isLoading = false
                )
            }
        }
    }

    // 플랜 데이터 로드 메서드 추가
    private suspend fun loadCurrentPlanData() {
        val planIdInt = tokenManager.getPlanId() // Int로 받기

        // 플랜이 없을 경우 바로 리턴
        if (planIdInt == -1 || !tokenManager.hasPlan()) {
            _uiState.value = _uiState.value.copy(
                currentPlanData = null,
                isPlanLoading = false
            )
            return
        }

        val planId = planIdInt.toLong()

        _uiState.value = _uiState.value.copy(isPlanLoading = true)

        val result = homeRepository.getCurrentPlan(planId)
        when (result) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    currentPlanData = result.data,
                    isPlanLoading = false,
                    error = null
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    currentPlanData = null,
                    isPlanLoading = false,
                    error = result.message
                )
            }
            is NetworkResult.Loading -> {
                // 로딩 상태를 UI에서 보여주고 싶으면 여기서 처리
                _uiState.value = _uiState.value.copy(isPlanLoading = true)
            }
        }
    }


    private suspend fun loadTodayExpenseData() {
        // 로딩 상태 시작
        _uiState.value = _uiState.value.copy(isTodayExpenseLoading = true)

        val familyName = authViewModel.getGroupName() ?: tokenManager.getGroupName() ?: "가족"
        val members = tokenManager.getGroupMembers().map { it.username }

        val todayExpenseResult = homeRepository.getTodayExpense()
        val lastUpdatedResult = homeRepository.getLastUpdated()

        when {
            todayExpenseResult is NetworkResult.Success && lastUpdatedResult is NetworkResult.Success -> {
                val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                val formattedAmount = numberFormat.format(todayExpenseResult.data.todayExpense)

                val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                val targetFormat = SimpleDateFormat("yy.MM.dd HH:mm", Locale.KOREA)
                val formattedDate = try {
                    val date = originalFormat.parse(lastUpdatedResult.data.lastUpdated)
                    targetFormat.format(date!!)
                } catch (e: Exception) {
                    lastUpdatedResult.data.lastUpdated
                }

                val todayExpense = TodayExpenseData(
                    familyName = familyName,
                    members = members,
                    updatedAt = formattedDate,
                    totalAmount = formattedAmount,
                    isHidden = false
                )

                _uiState.value = _uiState.value.copy(
                    todayExpenseData = todayExpense,
                    isTodayExpenseLoading = false, // 로딩 완료
                    error = null
                )
            }

            todayExpenseResult is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    error = todayExpenseResult.message,
                    isTodayExpenseLoading = false // 에러 시에도 로딩 완료
                )
            }

            else -> {
                _uiState.value = _uiState.value.copy(
                    error = "데이터를 불러올 수 없습니다.",
                    isTodayExpenseLoading = false // 에러 시에도 로딩 완료
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadSpendingTrendData() {
        val result = homeRepository.getSpendingTrend()

        when (result) {
            is NetworkResult.Success -> {
                val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                val formattedTodayAmount = numberFormat.format(result.data.todayAmount)

                _uiState.value = _uiState.value.copy(
                    spendingTrendData = result.data,
                    formattedTodayAmount = "${formattedTodayAmount}원",
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
                    error = "지출 추이 데이터를 불러올 수 없습니다.",
                    isLoading = false
                )
            }
        }
    }

    private fun loadFamilyDepositData() {
        // TokenManager에서 그룹 정보 가져오기
        val savingAccountNo = tokenManager.getGroupSavingAccountNo() ?: "9994559650565156"
        val amount = tokenManager.getGroupAmount()

        val familyDepositData = FamilyDepositData(
            savingAccountNo = savingAccountNo,
            amount = amount,
            isAmountHidden = false
        )

        _uiState.value = _uiState.value.copy(
            familyDepositData = familyDepositData
        )
    }

    private fun loadAccountData() {
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        _uiState.value = _uiState.value.copy(
            balance = numberFormat.format(1250000)
        )
    }

    fun refreshTodayExpense() {
        viewModelScope.launch {
            loadTodayExpenseData() // 이미 내부에서 로딩 상태를 관리하므로 중복 설정 제거
        }
    }

    fun refreshSpendingTrend() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadSpendingTrendData()
        }
    }

    // 플랜 데이터 새로고침 메서드 추가
    fun refreshCurrentPlan() {
        viewModelScope.launch {
            loadCurrentPlanData()
        }
    }

    fun toggleAmountVisibility() {
        val currentData = _uiState.value.todayExpenseData
        val updatedData = currentData?.copy(isHidden = !currentData.isHidden)
        _uiState.value = _uiState.value.copy(todayExpenseData = updatedData)
    }

    fun toggleDepositAmountVisibility() {
        val currentData = _uiState.value.familyDepositData
        val updatedData = currentData?.copy(isAmountHidden = !currentData.isAmountHidden)
        _uiState.value = _uiState.value.copy(familyDepositData = updatedData)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isTodayExpenseLoading: Boolean = false, // 추가
    val balance: String = "0",
    val todayExpenseData: TodayExpenseData? = null,
    val spendingTrendData: SpendingTrendData? = null,
    val familyDepositData: FamilyDepositData? = null,
    val formattedTodayAmount: String = "0원",
    val currentPlanData: CurrentPlanResponse? = null,
    val isPlanLoading: Boolean = false,
    val error: String? = null
)

data class TodayExpenseData(
    val familyName: String,
    val members: List<String>,
    val updatedAt: String,
    val totalAmount: String,
    val isHidden: Boolean = false
)

data class FamilyDepositData(
    val savingAccountNo: String,
    val amount: Int,
    val isAmountHidden: Boolean = false
)