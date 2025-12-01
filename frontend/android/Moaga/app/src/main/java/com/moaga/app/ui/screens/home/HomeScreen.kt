package com.moaga.app.ui.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moaga.app.data.local.TokenManager
import com.moaga.app.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()

    // HomeViewModel 생성 시 context와 authViewModel 전달
    val homeViewModel: HomeViewModel = remember {
        HomeViewModel(context, authViewModel)
    }

    val uiState by homeViewModel.uiState.collectAsState()

    // 사용자 정보가 없으면 자동으로 조회
    LaunchedEffect(Unit) {
        if (!authViewModel.hasUserInfo()) {
            authViewModel.fetchUserInfo()
        }
    }

    val username = authViewModel.getCurrentUsername() ?: "사용자"

    // HomeScreen이 처음 렌더링될 때 토큰 로그 출력
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()
        val userId = tokenManager.getUserId()
        val username = tokenManager.getUsername()
        val groupname = tokenManager.getGroupName()
        val members = tokenManager.getGroupMembers()

        Log.d("HomeScreen", "=== 저장된 토큰 정보 ===")
        Log.d("HomeScreen", "Access Token: $accessToken")
        Log.d("HomeScreen", "User ID: $userId")
        Log.d("HomeScreen", "group ID: $groupname")
        Log.d("HomeScreen", "Username: $username")
        Log.d("HomeScreen", "로그인 상태: ${tokenManager.isLoggedIn()}")
        Log.d("HomeScreen", "========================")
        members.forEach {
            Log.d("HomeScreen", "그룹멤버: ${it.username} (${it.displayname}) / memberId=${it.memberId}")
        }

        accessToken?.let { token ->
            val preview = if (token.length > 20) {
                "${token.substring(0, 20)}..."
            } else {
                token
            }
            Log.d("HomeScreen", "Token Preview: $preview")
        }
    }

    // 에러 처리
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Log.e("HomeScreen", "Error: $error")
            homeViewModel.clearError()
        }
    }

    // LazyColumn 스크롤 상태
    val scrollState = rememberLazyListState()

    // 스크롤 상태에 따른 흰 박스 위치 계산
    val isScrolled by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0
        }
    }

    // 스크롤 상태 감지
    val isScrolling by remember {
        derivedStateOf {
            scrollState.isScrollInProgress
        }
    }

    // 애니메이션된 탑 패딩 (박스 상단만 올라가도록)
    val whiteBoxTopPadding by animateDpAsState(
        targetValue = if (isScrolled) 35.dp else 328.dp,
        animationSpec = tween(durationMillis = 300),
        label = "whiteBoxTopPadding"
    )

    // 바텀시트가 완전히 내려가 있는지 확인 - 두 번째 파일에서 가져온 개선사항
    val isBottomSheetCollapsed by remember {
        derivedStateOf {
            whiteBoxTopPadding.value >= 320f // 약간의 여유를 둠
        }
    }

    // FAB 표시 여부 결정 - 두 번째 파일의 개선된 로직 적용
    // 스크롤 중이 아니고 바텀시트가 완전히 내려가 있을 때만 표시
    val showFab by remember {
        derivedStateOf {
            !isScrolling && isBottomSheetCollapsed
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18A87E))
    ) {
        // 하단 스크롤 영역
        HomeBottomSheet(
            scrollState = scrollState,
            whiteBoxTopPadding = whiteBoxTopPadding,
            uiState = uiState,
            homeViewModel = homeViewModel,
            navController = navController,
            // 두 번째 파일에서 가져온 플랜 네비게이션 기능
            onNavigateToPlan = {
                // 플랜 탭으로 이동 (MainActivity의 selectedTabIndex를 3으로 변경)
                navController.navigate("plan") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )

        // 상단 헤더 - NavController 전달하여 알림 네비게이션 기능 포함
        HomeHeader(
            username = username,
            uiState = uiState,
            homeViewModel = homeViewModel,
            navController = navController // 첫 번째 파일의 네비게이션 기능 유지
        )

        // 플로팅 버튼 - 개선된 표시 조건 적용
        HomeFloatingButton(
            showFab = showFab,
            navController = navController
        )
    }
}