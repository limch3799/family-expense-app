package com.moaga.app.ui.screens.analysis

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.R
import com.moaga.app.data.local.TokenManager
import com.moaga.app.data.api.dto.request.report.GroupTotalExpenseRequest
import com.moaga.app.data.api.dto.request.report.ReportsListRequest
import com.moaga.app.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FamilyGroupInfo(
    val groupName: String,
    val members: List<MemberInfo>
)

data class MemberInfo(
    val name: String,
    val profileImageRes: Int
)

data class GroupExpenseInfo(
    val totalAmount: Int,
    val startDate: String,
    val endDate: String,
    val changeRate: String,
    val changeType: String,
    val transactionCount: Int
)

data class ReportItem(
    val aiReportId: Int,
    val displayTitle: String
)

class AnalysisViewModel : ViewModel() {

    private val reportRepository = ReportRepository()

    private val _familyGroupInfo = MutableStateFlow<FamilyGroupInfo?>(null)
    val familyGroupInfo: StateFlow<FamilyGroupInfo?> = _familyGroupInfo.asStateFlow()

    private val _groupExpenseInfo = MutableStateFlow<GroupExpenseInfo?>(null)
    val groupExpenseInfo: StateFlow<GroupExpenseInfo?> = _groupExpenseInfo.asStateFlow()

    private val _recentReports = MutableStateFlow<List<ReportItem>>(emptyList())
    val recentReports: StateFlow<List<ReportItem>> = _recentReports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 프로필 이미지 순환용 리스트
    private val profileImages = listOf(
        R.drawable.profile_character_green,
        R.drawable.profile_character_pink,
        R.drawable.profile_character_orange,
        R.drawable.profile_character_purple
    )

    fun loadFamilyGroupInfo(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val tokenManager = TokenManager(context)
                val groupName = tokenManager.getGroupName() ?: "가족그룹"
                val groupMembers = tokenManager.getGroupMembers()

                val memberInfoList = groupMembers.mapIndexed { index, member ->
                    MemberInfo(
                        name = member.username ?: "멤버${index + 1}",
                        profileImageRes = profileImages[index % profileImages.size]
                    )
                }

                _familyGroupInfo.value = FamilyGroupInfo(
                    groupName = groupName,
                    members = memberInfoList
                )

                // 그룹 지출 정보와 리포트 목록 동시 로드
                loadGroupExpenseInfo(context)
                loadRecentReports(context)

            } catch (e: Exception) {
                _error.value = "데이터를 불러오는데 실패했습니다: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadGroupExpenseInfo(context: Context) {
        try {
            val tokenManager = TokenManager(context)
            val groupId = tokenManager.getGroupId()
            val currentYearMonth = getCurrentYearMonth()

            val request = GroupTotalExpenseRequest(
                groupId = groupId,
                yearMonth = currentYearMonth
            )

            val response = reportRepository.getGroupTotalExpense(request)
            val (startDate, endDate) = getCurrentMonthDateRange()

            _groupExpenseInfo.value = GroupExpenseInfo(
                totalAmount = response.currentPeriod.totalAmount,
                startDate = startDate,
                endDate = endDate,
                changeRate = response.changeRate,
                changeType = response.changeType,
                transactionCount = response.currentPeriod.transactionCount
            )

        } catch (e: Exception) {
            _error.value = "지출 정보를 불러오는데 실패했습니다: ${e.message}"
            e.printStackTrace()
        }
    }

    private suspend fun loadRecentReports(context: Context) {
        try {
            val tokenManager = TokenManager(context)
            val groupId = tokenManager.getGroupId()

            val request = ReportsListRequest(groupId = groupId)
            val response = reportRepository.getReportsList(request)

            val reportItems = response.map { report ->
                ReportItem(
                    aiReportId = report.aiReportId,
                    displayTitle = formatYearMonth(report.yearMonth)
                )
            }

            _recentReports.value = reportItems

        } catch (e: Exception) {
            _error.value = "리포트 목록을 불러오는데 실패했습니다: ${e.message}"
            e.printStackTrace()
        }
    }

    // 현재 년월을 YYYY-MM 형식으로 반환 (public으로 변경)
    fun getCurrentYearMonth(): String {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    // 현재 월의 1일부터 오늘까지 날짜 범위 반환
    private fun getCurrentMonthDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // 이번 달 1일
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = calendar.time

        val dateFormat = SimpleDateFormat("MM.dd", Locale.getDefault())

        return Pair(
            dateFormat.format(firstDay),
            dateFormat.format(today)
        )
    }

    // YYYY-MM을 "YYYY년 MM월" 형식으로 변환
    private fun formatYearMonth(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0]
            val month = parts[1].toInt() // 앞의 0 제거
            "${year}년 ${month}월"
        } catch (e: Exception) {
            yearMonth
        }
    }

    // 에러 상태 초기화
    fun clearError() {
        _error.value = null
    }

    // 데이터 새로고침
    fun refresh(context: Context) {
        loadFamilyGroupInfo(context)
    }
}