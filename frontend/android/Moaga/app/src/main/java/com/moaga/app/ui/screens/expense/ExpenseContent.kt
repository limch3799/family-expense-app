package com.moaga.app.ui.screens.expense

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moaga.app.R
import com.moaga.app.data.api.ApiClient
import com.moaga.app.ui.components.expense.ExpenseCalendar
import com.moaga.app.ui.components.expense.ExpenseChart
import com.moaga.app.ui.components.expense.ExpenseItemComponent
import com.moaga.app.ui.components.home.noRippleClickable
import com.moaga.app.ui.theme.font_gothic_3
import com.moaga.app.ui.theme.font_gothic_5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseContent(
    listState: LazyListState,
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    scope: CoroutineScope,
    currentMonth: YearMonth,
    selectedTab: Int,
    selectedDate: LocalDate?,
    today: LocalDate,
    isCurrentMonth: Boolean,
    forceShowLoading: Boolean,
    showInitialLoading: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onTabChange: (Int) -> Unit,
    onDateSelected: (LocalDate?) -> Unit
) {
    // 바텀시트 상태 관리
    var selectedExpenseItem by remember { mutableStateOf<ExpenseItem?>(null) }

    // 스크롤 위치 저장을 위한 키
    val contentKey by remember(currentMonth, selectedTab) {
        mutableStateOf("${currentMonth}_$selectedTab")
    }

    // 로딩 상태가 변경될 때 스크롤 위치 보존
    val preservedScrollPosition = remember { mutableStateOf(0) }
    val preservedScrollOffset = remember { mutableStateOf(0) }

    val ctx = LocalContext.current
    var isRotating by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    var internalUpdatedAt by remember { mutableStateOf(uiState.lastUpdated) }
    val displayTime by remember(uiState.lastUpdated) {
        derivedStateOf {
            if (uiState.lastUpdated.isNotEmpty()) {
                formatRelativeTime(uiState.lastUpdated)
            } else {
                "로딩 중..."
            }
        }
    }

    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "refreshRotation"
    )

    // 차트 모드로 전환할 때 오늘 날짜 거래내역 자동 로드
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && !showInitialLoading) { // 차트 모드
            onDateSelected(today) // 오늘 날짜로 선택 상태 업데이트
            viewModel.loadDailyTransactions(today) // 오늘 거래내역 로드
        }
    }

    LaunchedEffect(forceShowLoading) {
        if (forceShowLoading && !showInitialLoading) {
            // 로딩 시작 시 현재 스크롤 위치 저장
            preservedScrollPosition.value = listState.firstVisibleItemIndex
            preservedScrollOffset.value = listState.firstVisibleItemScrollOffset
        } else if (!forceShowLoading && !showInitialLoading && preservedScrollPosition.value > 0) {
            // 로딩 종료 시 스크롤 위치 복원
            try {
                listState.scrollToItem(preservedScrollPosition.value, preservedScrollOffset.value)
            } catch (e: Exception) {
                // 복원 실패 시 무시
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 고정 헤더
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "가족 지출 내역",
                    fontSize = 24.sp,
                    fontFamily = font_gothic_5,
                    lineHeight = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        // 스크롤 가능한 컨텐츠
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 0.dp)
        ) {
            item(key = "header_$contentKey") {
                // 토글 이미지 버튼과 업데이트 시간
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onTabChange(if (selectedTab == 0) 1 else 0)
                            }
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (selectedTab == 0) R.drawable.button_toggle_calendar
                                else R.drawable.button_toggle_chart
                            ),
                            contentDescription = if (selectedTab == 0) "캘린더 토글" else "차트 토글",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(36.dp)
                        )
                    }

                    // 업데이트 시간
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayTime,
                            fontSize = 10.sp,
                            color = Color(0xFF666666),
                            fontFamily = font_gothic_3
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(rotationZ = rotation % 360)
                                .noRippleClickable {
                                    scope.launch {
                                        rotationTarget += 720f
                                        isRotating = true
                                        try {
                                            val res = ApiClient.apiService.syncTransactions()
                                            if (res.isSuccessful) {
                                                val timeRes = ApiClient.apiService.getGroupLastUpdated()
                                                if (timeRes.isSuccessful) {
                                                    val time = timeRes.body()?.lastUpdated ?: internalUpdatedAt
                                                    internalUpdatedAt = time
                                                }
                                                viewModel.refreshLastUpdated(currentMonth)
                                            } else {
                                                Toast.makeText(ctx, "실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isRotating = false
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            // 캘린더를 선택했을 때만 월 네비게이션 표시
            if (selectedTab == 0) {
                item(key = "month_nav_$contentKey") {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        // 연도 네비게이션
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "이전 연도",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (currentMonth.year > 2022) {
                                            val newYear = currentMonth.year - 1
                                            val newMonth = if (newYear == today.year) today.monthValue else 12
                                            onMonthChange(YearMonth.of(newYear, newMonth))
                                        }
                                    }
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${currentMonth.year}",
                                fontSize = 18.sp,
                                fontFamily = font_gothic_5,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            if (currentMonth.year < today.year) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "다음 연도",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val newYear = currentMonth.year + 1
                                            val newMonth = if (newYear == today.year) today.monthValue else 12
                                            onMonthChange(YearMonth.of(newYear, newMonth))
                                        }
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 월 탭 레이아웃
                        val months = (1..12).filter { month ->
                            val yearMonth = YearMonth.of(currentMonth.year, month)
                            !yearMonth.isAfter(YearMonth.from(today))
                        }

                        val lazyRowState = rememberLazyListState()

                        LaunchedEffect(currentMonth) {
                            val selectedIndex = months.indexOf(currentMonth.monthValue)
                            if (selectedIndex != -1) {
                                lazyRowState.animateScrollToItem(maxOf(0, selectedIndex - 1))
                            }
                        }

                        LazyRow(
                            state = lazyRowState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(months, key = { index, month -> "month_${month}_$index" }) { index, month ->
                                val isSelected = month == currentMonth.monthValue

                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            onMonthChange(YearMonth.of(currentMonth.year, month))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${month}월",
                                        fontSize = 14.sp,
                                        fontFamily = font_gothic_3,
                                        color = if (isSelected) Color(0xFF18A87E) else Color(0xFF666666),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)
                                            .background(
                                                color = if (isSelected) Color(0xFF18A87E) else Color.Transparent
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "main_content_$contentKey") {
                // 캘린더 또는 차트 표시
                if (selectedTab == 0) {
                    ExpenseCalendar(
                        currentMonth = currentMonth,
                        today = today,
                        monthlyExpenseData = uiState.monthlyExpenseData,
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            onDateSelected(date)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    ExpenseChart(
                        chartData = uiState.chartData,
                        onDateSelected = { date ->
                            onDateSelected(date)
                            viewModel.loadDailyTransactions(date)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(key = "spacer_$contentKey") {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item(key = "divider_$contentKey") {
                // 연한 회색 구분선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color(0xFFF1F1F1))
                )
            }

            item(key = "expense_title_$contentKey") {
                // 지출내역 제목 + 건수 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "지출내역",
                        fontSize = 16.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )

                    if (uiState.transactionCount > 0 && !forceShowLoading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.transactionCount}건",
                            fontSize = 14.sp,
                            fontFamily = font_gothic_3,
                            color = Color(0xFF18A87E)
                        )
                    }
                }
            }

            // 로딩 상태 처리
            if (forceShowLoading) {
                item(key = "loading_$contentKey") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.loading_block)
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(256.dp)
                        )
                    }
                }
            } else {
                // 지출 내역 아이템들 - 고유한 키 생성 (인덱스 포함)
                itemsIndexed(
                    items = uiState.expenseItems,
                    key = { index, item ->
                        // 항목 ID가 있다면 ID 사용, 없다면 인덱스를 포함한 복합 키 생성
                        if (item.id > 0) {
                            "expense_item_${item.id}"
                        } else {
                            "expense_item_${index}_${item.date}_${item.time}_${item.name}_${item.amount}_${item.person}"
                        }
                    }
                ) { index, item ->
                    ExpenseItemComponent(
                        item = item,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            selectedExpenseItem = item
                        }
                    )
                }

                // 데이터가 없을 때
                if (uiState.expenseItems.isEmpty() && selectedDate != null) {
                    item(key = "no_data_$contentKey") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "선택한 날짜에 지출 내역이 없습니다.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontFamily = font_gothic_3
                            )
                        }
                    }
                }
            }

            item(key = "bottom_spacer_$contentKey") {
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
    }

    // 바텀시트
    ExpenseItemBottomSheet(
        item = selectedExpenseItem,
        onDismiss = { selectedExpenseItem = null },
        currentUserName = viewModel.currentUserName.orEmpty(),
        onCategoryChange = { category ->
            println("카테고리 변경: ${category.name}")
            selectedExpenseItem = selectedExpenseItem?.copy(category = category.name)
        },
        onExcludeToggle = { isExcluded ->
            println("지출내역 제외: $isExcluded")
        },
        onSaveCategory = { transactionId, categoryId, categoryName, exclude ->
            println("🔥 바텀시트에서 저장 요청: ID=$transactionId, exclude=$exclude") // 디버그 로그

            viewModel.changeCategoryAndExclude(
                transactionId = transactionId,
                categoryId = categoryId,
                newCategoryName = categoryName,
                exclude = exclude,
                currentMonth = currentMonth,
                selectedDate = selectedDate, // ✅ 현재 선택된 날짜 전달
                onComplete = {
                    println("🔥 변경 완료 - 바텀시트 닫기") // 디버그 로그
                    selectedExpenseItem = null
                }
            )
        }
    )
}

fun formatRelativeTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul") // ✅ 한국 시간대 적용
        val parsedDate = sdf.parse(timestamp) ?: return timestamp

        val diffMillis = System.currentTimeMillis() - parsedDate.time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)

        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(parsedDate)
        }
    } catch (e: Exception) {
        timestamp // 실패하면 원본 반환
    }
}