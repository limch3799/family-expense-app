package com.moaga.app.ui.screens.expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moaga.app.R
import com.moaga.app.ui.components.expense.MyExpenseCalendar
import com.moaga.app.ui.components.expense.MyExpenseChart
import com.moaga.app.ui.components.expense.MyExpenseItemComponent
import com.moaga.app.ui.theme.font_gothic_3
import com.moaga.app.ui.theme.font_gothic_5
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyExpenseScreen(navController: NavController) {
    val viewModel: MyExpenseViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 캘린더, 1: 차트
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val today = LocalDate.now()

    // 월 변경시 데이터 로드
    LaunchedEffect(currentMonth) {
        viewModel.loadMonthlyCalendar(currentMonth)
    }

    // 날짜 선택시 거래내역 로드
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            viewModel.loadDailyTransactions(date)
        }
    }

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadMonthlyCalendar(currentMonth)
        viewModel.loadSpendingTrend()
        viewModel.loadDailyTransactions(today)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // 뒤로가기 버튼과 제목
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigateUp()
                                }
                        )

                        Text(
                            text = "나의 지출 내역",
                            fontSize = 18.sp,
                            fontFamily = font_gothic_5,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 스크롤 가능한 컨텐츠
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    // 토글 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    selectedTab = if (selectedTab == 0) 1 else 0
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
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 캘린더 또는 차트 표시
                    if (selectedTab == 0) {
                        MyExpenseCalendar(
                            currentMonth = currentMonth,
                            today = today,
                            monthlyExpenseData = uiState.monthlyExpenseData,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                selectedDate = date
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        MyExpenseChart(
                            chartData = uiState.chartData,
                            onDateSelected = { date ->
                                selectedDate = date
                                viewModel.loadDailyTransactions(date)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // 구분선
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color(0xFFF1F1F1))
                    )
                }

                item {
                    // 지출내역 제목
                    Text(
                        text = "지출내역",
                        fontSize = 16.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // 지출 내역 아이템들
                items(uiState.expenseItems) { item ->
                    MyExpenseItemComponent(
                        item = item,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    // 바텀 네비게이션을 위한 여백
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}