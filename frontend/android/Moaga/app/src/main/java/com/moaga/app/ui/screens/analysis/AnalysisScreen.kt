package com.moaga.app.ui.screens.analysis

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.moaga.app.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6
import kotlinx.coroutines.delay

@Composable
fun AnalysisScreen(
    onNavigateToReport: (reportId: Int?, reportType: Int?, date: String?) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val viewModel: AnalysisViewModel = viewModel()
    val familyGroupInfo by viewModel.familyGroupInfo.collectAsState()
    val groupExpenseInfo by viewModel.groupExpenseInfo.collectAsState()
    val recentReports by viewModel.recentReports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 로딩 상태 관리 (UI 로딩과 별개)
    var isUILoading by remember { mutableStateOf(true) }

    // 바텀시트 상태 관리
    var showBottomSheet by remember { mutableStateOf(false) }

    // 메인 Lottie 애니메이션 상태 관리
    var isPlaying by remember { mutableStateOf(false) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.report))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = 1
    )

    // 로딩 애니메이션
    val loadingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_dot))
    val loadingProgress by animateLottieCompositionAsState(
        composition = loadingComposition,
        isPlaying = isUILoading,
        iterations = LottieConstants.IterateForever
    )

    // 이번달 지출현황 애니메이션
    val thisMonthComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.thismonth))
    val thisMonthProgress by animateLottieCompositionAsState(
        composition = thisMonthComposition,
        isPlaying = !isUILoading,
        iterations = LottieConstants.IterateForever
    )

    // 전체 리포트 애니메이션
    val allReportComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.allreport))
    val allReportProgress by animateLottieCompositionAsState(
        composition = allReportComposition,
        isPlaying = !isUILoading,
        iterations = LottieConstants.IterateForever
    )

    // 데이터 로드 및 로딩 상태 제어
    LaunchedEffect(Unit) {
        viewModel.loadFamilyGroupInfo(context)
        delay(1000)
        isUILoading = false
    }

    // 메인 애니메이션 시작 제어
    LaunchedEffect(Unit) {
        delay(1000)
        isPlaying = true
        delay(13000)
        isPlaying = false
    }

    // 카드 등장 애니메이션 상태
    var showCards by remember { mutableStateOf(false) }
    LaunchedEffect(isUILoading) {
        if (!isUILoading) {
            showCards = true
        }
    }

    // 에러 처리
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 여기서 토스트나 스낵바로 에러 표시
            viewModel.clearError()
        }
    }

    // API에서 가져온 리포트들을 AnalysisCard로 변환
    val cards = remember(recentReports) {
        val colors = listOf(
            Color(0xFF7CC1F6) to Color(0xFF6CBEFD),
            Color(0xFFB1F574) to Color(0xFF78D426),
            Color(0xFFFADDC7) to Color(0xFFF39043),
            Color(0xFFE9C1FA) to Color(0xFFC54CFA),
            Color(0xFFF8C1D4) to Color(0xFFF5397A)
        )
        val icons = listOf(
            Icons.Default.TrendingUp,
            Icons.Default.BarChart,
            Icons.Default.Assessment,
            Icons.Default.PieChart,
            Icons.Default.Analytics
        )

        recentReports.mapIndexed { index, report ->
            val (bgColor, textColor) = colors[index % colors.size]
            AnalysisCard(
                title = report.displayTitle,
                description = "총 지출액",
                value = "리포트 확인",
                icon = icons[index % icons.size],
                color = bgColor,
                textColor = textColor
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isUILoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFFFFF))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))

                    // 제목 + 애니메이션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "지출 분석 리포트",
                                fontSize = 21.sp,
                                fontFamily = font_gothic_5,
                                lineHeight = 18.sp,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(36.dp))
                            Text(
                                text = "  가족 소비 패턴을 상세히 분석하여",
                                fontSize = 10.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "  지출 관리를 위한 인사이트를",
                                fontSize = 10.sp,
                                color = Color(0xFF666666)
                            )
                        }

                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .size(160.dp)
                                .offset(y = (-20).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height((-10).dp))

                    Box(
                        modifier = Modifier.offset(y = (-20).dp)
                    ) {
                        FamilyReportCard(
                            groupInfo = familyGroupInfo,
                            expenseInfo = groupExpenseInfo
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 이번 달 지출 현황 카드
                    AnimatedSlideUp(visible = showCards, delayMillis = 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    // ViewModel의 getCurrentYearMonth 함수 결과 사용
                                    val currentYearMonth = viewModel.getCurrentYearMonth()
                                    onNavigateToReport(null, 1, currentYearMonth)
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF7F0FF))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFD4A2F1).copy(alpha = 0.4f),
                                                Color(0xFFD4A2F1).copy(alpha = 0.2f),
                                                Color.Transparent
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(
                                                x = Float.POSITIVE_INFINITY * 0.85f,
                                                y = Float.POSITIVE_INFINITY * 0.85f
                                            ),
                                            radius = Float.POSITIVE_INFINITY * 0.5f
                                        )
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(20.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "이번 달 지출 현황",
                                            fontSize = 16.sp,
                                            fontFamily = font_paperlogy_6,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "가족의 이번 달 소비 패턴을",
                                            fontSize = 10.sp,
                                            color = Color(0xFF555555)
                                        )
                                        Text(
                                            text = "분석해 드릴게요",
                                            fontSize = 10.sp,
                                            color = Color(0xFF555555)
                                        )
                                    }

                                    LottieAnimation(
                                        composition = thisMonthComposition,
                                        progress = { thisMonthProgress },
                                        modifier = Modifier
                                            .size(180.dp)
                                            .offset(x = 25.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 전체 리포트 카드
                    AnimatedSlideUp(visible = showCards, delayMillis = 150) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showBottomSheet = true
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFEDFFED))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF5AEC60).copy(alpha = 0.4f),
                                                Color(0xFF5AEC60).copy(alpha = 0.2f),
                                                Color.Transparent
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(
                                                x = Float.POSITIVE_INFINITY * 0.85f,
                                                y = Float.POSITIVE_INFINITY * 0.85f
                                            ),
                                            radius = Float.POSITIVE_INFINITY * 0.5f
                                        )
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(20.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "전체 리포트",
                                            fontSize = 18.sp,
                                            fontFamily = font_paperlogy_6,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "월별 지출 내역과 트렌드를",
                                            fontSize = 12.sp,
                                            color = Color(0xFF555555)
                                        )
                                        Text(
                                            text = "확인해보세요",
                                            fontSize = 12.sp,
                                            color = Color(0xFF555555)
                                        )
                                    }

                                    LottieAnimation(
                                        composition = allReportComposition,
                                        progress = { allReportProgress },
                                        modifier = Modifier
                                            .size(160.dp)
                                            .offset(x = 20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedSlideUp(visible = showCards, delayMillis = 300) {
                        RecentReportsSection(
                            cards = cards, // analysisCards 대신 cards 사용
                            recentReports = recentReports,
                            onReportClick = { reportId, reportType, date ->
                                // onNavigateToReport 함수 사용
                                onNavigateToReport(reportId, reportType, date)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(140.dp))
                }
            }
        }

        if (isUILoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = loadingComposition,
                    progress = { loadingProgress },
                    modifier = Modifier.size(160.dp)
                )
            }
        }

        if (showBottomSheet) {
            ReportFilterBottomSheet(
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

/**
 * 아래에서 위로 슬라이드 업 + 페이드인 애니메이션
 */
@Composable
fun AnimatedSlideUp(
    visible: Boolean,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var localVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMillis.toLong())
            localVisible = true
        } else {
            localVisible = false
        }
    }

    // 애니메이션 속도 느리게 (1.2초)
    val alpha by animateFloatAsState(
        targetValue = if (localVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200) // 페이드인 1.2초
    )

    val translateY by animateFloatAsState(
        targetValue = if (localVisible) 0f else 50f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing) // 천천히 올라옴
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = translateY
        }
    ) {
        content()
    }
}