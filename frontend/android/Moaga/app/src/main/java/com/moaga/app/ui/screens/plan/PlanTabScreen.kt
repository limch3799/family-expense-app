// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanTabScreen.kt
package com.moaga.app.ui.screens.plan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moaga.app.R
import com.moaga.app.data.local.TokenManager
import com.moaga.app.data.repository.PlanRepository
import com.moaga.app.ui.theme.font_gothic_5

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlanTabScreen(
    onOpenHistory: () -> Unit = {},
    onOpenPastPlanDetail: (String) -> Unit = {},
    apiService: com.moaga.app.data.api.ApiService
) {
    val primaryGreen = Color(0xFF18A87E)
    val titleColor = Color(0xFF111111)
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("입금 내역", "지난 플랜", "플랜 상품")

    // 초기 로딩 상태 관리
    var showInitialLoading by remember { mutableStateOf(true) }

    // 초기 로딩 처리 (화면 진입 시)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // 1초 후 초기 로딩 숨김
        showInitialLoading = false
    }

    // TokenManager를 통해 planId 확인
    val tokenManager = remember { TokenManager(context) }
    val hasPlan = tokenManager.hasPlan()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // planId가 없으면 "플랜 없음" 상태 표시
        if (!hasPlan) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "진행 중인 플랜이 없습니다.",
                    fontSize = 16.sp,
                    color = Color(0xFF6C7682)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "새로운 플랜을 생성해보세요!",
                    fontSize = 14.sp,
                    color = Color(0xFF9AA2A9)
                )
            }
        } else {
            // ViewModel 생성 (planId가 있을 때만)
            val viewModel: PlanViewModel = viewModel {
                PlanViewModel(
                    planRepository = PlanRepository(apiService),
                    tokenManager = tokenManager
                )
            }

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Spacer(Modifier.height(54.dp))

                // 저축 플랜 타이틀
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "가족 플랜",
                        fontSize = 26.sp,
                        fontFamily = font_gothic_5,
                        lineHeight = 18.sp,
                        color = Color(0xFF1A1A1A)
                    )
                }
                Spacer(Modifier.height(8.dp))

                // 진행 중인 플랜 상품 섹션 (onOpenPlan 파라미터 제거)
                CurrentPlanSection(
                    primaryGreen = primaryGreen,
                    titleColor = titleColor,
                    isLoading = uiState.isLoading,
                    planTitle = viewModel.getPlanTitle(),
                    currentAmount = viewModel.getFormattedCurrentAmount(),
                    targetAmount = viewModel.getFormattedTargetAmount(),
                    startDate = viewModel.getFormattedStartDate(),
                    dday = viewModel.getDdayText(),
                    progress = viewModel.getProgress()
                )

                Spacer(Modifier.height(16.dp))

                // 커스텀 탭 레이아웃
                CustomTabLayout(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                    primaryColor = primaryGreen,
                    titleColor = titleColor
                )

                Spacer(Modifier.height(16.dp))

                // 탭 컨텐츠
                when (selectedTabIndex) {
                    0 -> DepositHistoryContent(
                        primaryGreen = primaryGreen,
                        titleColor = titleColor,
                        transactions = viewModel.getFormattedTransactions()
                    )
                    1 -> PastPlanContent(onOpenPastPlanDetail = onOpenPastPlanDetail)
                    2 -> PlanProductsContent()
                }

                // 에러 상태 표시
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 로티 애니메이션 베어 캐릭터 (플로팅) - 초기 로딩 중이 아닐 때만 표시
            if (!showInitialLoading) {
                val bearComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bear))

                LottieAnimation(
                    composition = bearComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(96.dp)
                        .offset(x = (-24).dp, y = 52.dp)
                        .align(Alignment.TopEnd)
                        .zIndex(1f)
                )
            }
        }

        // 초기 로딩 오버레이
        PlanLoadingOverlay(showInitialLoading)
    }
}

@Composable
fun PlanLoadingOverlay(showLoading: Boolean) {
    if (showLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .zIndex(999f),
            contentAlignment = Alignment.Center
        ) {
            // 로딩 로티 애니메이션 (ExpenseScreen에서 사용하는 것과 동일하게)
            val loadingComposition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading_dot) // 로딩 애니메이션 리소스
            )

            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}