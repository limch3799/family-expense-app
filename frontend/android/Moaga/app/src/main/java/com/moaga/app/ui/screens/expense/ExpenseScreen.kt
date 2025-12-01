package com.moaga.app.ui.screens.expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseScreen() {
    val viewModel: ExpenseViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isFabExpanded by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 캘린더, 1: 차트
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    // 초기 로딩 상태 관리
    var showInitialLoading by remember { mutableStateOf(true) }

    // 최소 1초 로딩 유지 상태
    var forceShowLoading by remember { mutableStateOf(false) }
    var loadingStartTime by remember { mutableStateOf<Long?>(null) }

    // 데이터 로딩 중인지 추적하는 상태 추가
    var isDataChanging by remember { mutableStateOf(false) }

    // 초기 로딩 처리 (화면 진입 시)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        showInitialLoading = false
    }

    // 로딩 상태 변화 감지
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            loadingStartTime = System.currentTimeMillis()
            forceShowLoading = true
        } else {
            val elapsed = System.currentTimeMillis() - (loadingStartTime ?: 0L)
            if (elapsed < 1000) {
                kotlinx.coroutines.delay(1000 - elapsed)
            }
            forceShowLoading = false
            isDataChanging = false
        }
    }

    // 스크롤 상태 및 위치 감지
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                Float.MAX_VALUE
            }
        }
    }

    val isScrolling by remember {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }

    // FAB 표시 여부 결정
    val showFab by remember {
        derivedStateOf {
            !showInitialLoading && scrollOffset <= 600 // 픽셀 기준으로 조정
        }
    }

    val today = LocalDate.now()
    val isCurrentMonth = currentMonth == YearMonth.now()

    // 월 변경시 데이터 로드 - 개선된 버전
    LaunchedEffect(currentMonth) {
        if (!showInitialLoading) {
            isDataChanging = true
            // 트랜잭션 데이터만 먼저 클리어 (UI 깜빡임 방지)
            viewModel.clearTransactionData()
            viewModel.loadMonthlyCalendar(currentMonth)
        }
    }

    // 날짜 선택시 거래내역 로드 - 개선된 버전
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            if (!showInitialLoading && !isDataChanging) {
                viewModel.loadDailyTransactions(date)
            }
        }
    }

    // 에러 처리
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    // 스크롤이 시작되면 FAB 축소
    LaunchedEffect(isScrolling) {
        if (isScrolling && isFabExpanded) {
            isFabExpanded = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 메인 컨텐츠
        ExpenseContent(
            listState = listState,
            uiState = uiState,
            viewModel = viewModel,
            scope = scope,
            currentMonth = currentMonth,
            selectedTab = selectedTab,
            selectedDate = selectedDate,
            today = today,
            isCurrentMonth = isCurrentMonth,
            forceShowLoading = forceShowLoading,
            showInitialLoading = showInitialLoading,
            onMonthChange = { newMonth ->
                currentMonth = newMonth
                selectedDate = null // 날짜 선택 초기화
            },
            onTabChange = { newTab ->
                selectedTab = newTab
                // 차트 모드로 전환할 때는 날짜를 초기화하지 않음
                // 캘린더 모드로 전환할 때만 트랜잭션 데이터를 클리어
                if (newTab == 0) { // 캘린더 모드
                    selectedDate = null
                    scope.launch {
                        isDataChanging = true
                        viewModel.clearTransactionData()
                        viewModel.loadMonthlyCalendar(currentMonth)
                    }
                }
                // 차트 모드(newTab == 1)일 때는 ExpenseContent의 LaunchedEffect에서 처리
            },
            onDateSelected = { date ->
                selectedDate = date
            }
        )

        // 초기 로딩 오버레이
        ExpenseLoadingOverlay(showInitialLoading)

        // 배경 오버레이 및 FAB
        ExpenseFloatingComponents(
            showFab = showFab,
            isFabExpanded = isFabExpanded,
            onFabExpandToggle = { isFabExpanded = !isFabExpanded },
            onDismissExpanded = { isFabExpanded = false }
        )
    }
}