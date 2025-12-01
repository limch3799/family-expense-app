package com.moaga.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moaga.app.data.api.ApiClient.apiService
import com.moaga.app.data.api.dto.request.GroupInfoRequest
import com.moaga.app.ui.components.CustomBottomNavigation
import com.moaga.app.ui.screens.account.LinkedAccountsScreen
import com.moaga.app.ui.screens.analysis.AnalysisScreen
import com.moaga.app.ui.screens.analysis.report.ReportScreen
import com.moaga.app.ui.screens.deposit.FamilyDepositInfo
import com.moaga.app.ui.screens.expense.ExpenseScreen
import com.moaga.app.ui.screens.expense.MyExpenseScreen
import com.moaga.app.ui.screens.group.GroupInfoDetailScreen
import com.moaga.app.ui.screens.group.NoGroupScreen
import com.moaga.app.ui.screens.home.HomeScreen
import com.moaga.app.ui.screens.more.MoreScreen
import com.moaga.app.ui.screens.notification.NotificationSettingsScreen
import com.moaga.app.ui.screens.plan.PlanActivity
import com.moaga.app.ui.screens.plan.PlanEmptyScreen
import com.moaga.app.ui.screens.plan.PlanTabScreen
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        enableEdgeToEdge()
        window.navigationBarColor = Color.parseColor("#18A87E") // 네비게이션 바 색상
        setContent {
            MoagaTheme {
                MoagaApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoagaApp() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPlan by remember { mutableStateOf(false) }
    var planTabPage by rememberSaveable { mutableStateOf("home") }

    // 현재 라우트
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 최초 로딩 시 그룹/플랜 정보 조회 + 2초 최소 로딩 시간
    LaunchedEffect(Unit) {
        scope.launch {
            val startTime = System.currentTimeMillis()

            try {
                val user = apiService.getUserInfo()
                val groupId = user.groupId
                hasPlan = if (groupId != null) {
                    val groupInfo = apiService.getGroupInfo(GroupInfoRequest(groupId))
                    groupInfo.planId != null
                } else false
            } catch (e: Exception) {
                //Toast.makeText(ctx, "API 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

            // 2초 최소 로딩 시간 보장
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = 2000 - elapsedTime
            if (remainingTime > 0) {
                delay(remainingTime)
            }

            isLoading = false
        }
    }

    // 로딩 화면 (Lottie 애니메이션)
    if (isLoading) {
        LoadingScreen()
        return
    }

    // 현재 라우트에 따라 선택된 탭 변경
    LaunchedEffect(currentRoute) {
        selectedTabIndex = when (currentRoute) {
            "home" -> 0
            "expense_screen" -> 1
            "analysis" -> 2
            "plan" -> 3
            "more" -> 4
            else -> selectedTabIndex
        }
    }

    // 뒤로가기 핸들링
    BackHandler(enabled = currentRoute in listOf("family_deposit_info", "group_info_detail")) {
        navController.popBackStack()
    }

    // 바텀 네비게이션 표시 여부
    val hideBottomNavRoutes = listOf("family_deposit_info", "group_info_detail", "my_expense")
    val shouldShowBottomNav = currentRoute !in hideBottomNavRoutes &&
            !(currentRoute?.startsWith("report/") == true)

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("linked_accounts") {
                LinkedAccountsScreen(navController = navController)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("expense_screen") {
                ExpenseScreen()
            }
            composable("analysis") {
                AnalysisScreen(
                    onNavigateToReport = { reportId, reportType, date ->
                        navController.navigate("report/$reportId/$reportType/$date")
                    }
                )
            }
            composable("group_info_detail") {  // ✅ 여기에 이미 등록되어 있음
                GroupInfoDetailScreen(navController = navController)
            }
            // 보고서 화면
            composable("report/{reportId}/{reportType}/{date}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId")?.toIntOrNull()
                val reportType = backStackEntry.arguments?.getString("reportType")?.toIntOrNull()
                val date = backStackEntry.arguments?.getString("date")
                ReportScreen(
                    reportId = reportId,
                    reportType = reportType,
                    date = date,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("plan") {
                PlanTabScreen(

                    onOpenHistory = { /* 히스토리 화면으로 이동 */ },
                    onOpenPastPlanDetail = { planId -> /* 지난 플랜 상세로 이동 */ },
                    apiService = apiService // 여기서 apiService를 전달해야 합니다
                )
            }
            composable("more") {
                MoreScreen(navController = navController)
            }
            composable("family_deposit_info") {
                FamilyDepositInfo()
            }
            composable("group_info_detail") {
                GroupInfoDetailScreen(navController = navController)
            }
            composable("my_expense") {
                MyExpenseScreen(navController = navController)
            }
            composable("notification_settings") {
                NotificationSettingsScreen(navController = navController)
            }

            composable("no_group") { NoGroupScreen() }
        }

        if (shouldShowBottomNav) {
            CustomBottomNavigation(
                selectedIndex = selectedTabIndex,
                onItemSelected = { index ->
                    selectedTabIndex = index
                    val route = getRouteForIndex(index)
                    navController.navigate(route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                    if (index != 3) planTabPage = "home" // 플랜 탭 벗어나면 초기화
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_dot)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(180.dp)
        )
    }
}

// 인덱스에 따른 라우트 반환
private fun getRouteForIndex(index: Int): String {
    return when (index) {
        0 -> "home"
        1 -> "expense_screen"
        2 -> "analysis"
        3 -> "plan"
        4 -> "more"
        else -> "home"
    }
}