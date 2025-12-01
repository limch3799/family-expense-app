package com.moaga.app.ui.screens.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import com.moaga.app.MainActivity
import com.moaga.app.R
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.utils.NetworkResult
import com.moaga.app.ui.screens.group.NoGroupScreen
import com.moaga.app.ui.screens.pin.SimplePinLoginScreen
import com.moaga.app.ui.screens.signup.SignUpActivity
import com.moaga.app.ui.screens.splash.SplashScreen
import com.moaga.app.ui.theme.MoagaTheme
import com.moaga.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ApiClient 초기화
        ApiClient.initialize(this)

        setContent {
            MoagaTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                var showLoginScreen by rememberSaveable { mutableStateOf(false) }
                var showSimplePin by rememberSaveable { mutableStateOf(false) }
                var showNoGroup by rememberSaveable { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                var quickLoginEmail by rememberSaveable { mutableStateOf("") }

                // 저장된 이메일 확인 → 간편로그인 여부 결정
                val tokenManager = remember { ApiClient.getTokenManager() }
                LaunchedEffect(Unit) {
                    val savedEmail = tokenManager.getQuickLoginEmail().orEmpty()
                    if (savedEmail.isNotBlank()) {
                        quickLoginEmail = savedEmail
                        showSimplePin = true
                    } else {
                        showLoginScreen = true
                    }
                }

                LaunchedEffect(showSplash) {
                    if (!showSplash) {
                        setTheme(R.style.SplashTheme)
                    }
                }

                // 로그인 결과 관찰
                val loginResult by authViewModel.loginResult.observeAsState()

                // 로그인 결과 처리
                LaunchedEffect(loginResult) {
                    loginResult?.let { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                scope.launch {
                                    try {
                                        // 토큰 저장
                                        val loginRes = result.data  // 서버 응답(LoginResponse)라고 가정
                                        ApiClient.getTokenManager().saveTokens(
                                            accessToken = loginRes.accessToken,
                                            userId = loginRes.userId
                                        )

                                        // 간편 로그인용 이메일도 저장
                                        ApiClient.getTokenManager().setQuickLoginEnabled(true)
                                        ApiClient.getTokenManager().setQuickLoginEmail(quickLoginEmail)

                                        // 사용자 정보 가져오기
                                        val user = ApiClient.apiService.getUserInfo()
                                        Log.d(
                                            "LoginActivity",
                                            "로그인 성공: userId=${user.userId}, groupId=${user.groupId}"
                                        )

                                        if (user.groupId != null && user.groupId != 0) {
                                            Log.d("LoginActivity", "그룹 있음 → 메인 이동")
                                            goToMain()
                                        } else {
                                            Log.d("LoginActivity", "그룹 없음 → NoGroupScreen 이동")
                                            showSplash = false
                                            showLoginScreen = false
                                            showSimplePin = false
                                            showNoGroup = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("LoginActivity", "getUserInfo 실패: ${e.localizedMessage}", e)
                                        goToMain()
                                    }
                                }
                                authViewModel.clearLoginResult()
                            }

                            is NetworkResult.Error -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = result.message,
                                        withDismissAction = true
                                    )
                                }
                                authViewModel.clearLoginResult()
                            }

                            else -> {}
                        }
                    }
                }

                when {
                    showSplash -> {
                        SplashScreen(
                            onTimeout = { showSplash = false }
                        )
                    }

                    showNoGroup -> {
                        NoGroupScreen(
                            onBackToLogin = {
                                showNoGroup = false
                                showLoginScreen = true
                            }
                        )
                    }

                    showSimplePin -> {
                        var isSimpleLoading by remember { mutableStateOf(false) }

                        SimplePinLoginScreen(
                            email = quickLoginEmail,
                            isLoading = isSimpleLoading,
                            onBackToPasswordLogin = {
                                tokenManager.setQuickLoginEmail("")
                                quickLoginEmail = ""
                                showSimplePin = false
                                showLoginScreen = true
                            },
                            onPinCompleted = { pin ->
                                if (isSimpleLoading) return@SimplePinLoginScreen
                                isSimpleLoading = true
                                scope.launch {
                                    try {
                                        val res = ApiClient.apiService.loginWithSimple(
                                            com.moaga.app.data.api.dto.request.SimpleLoginRequest(
                                                email = quickLoginEmail,
                                                simplePassword = pin
                                            )
                                        )
                                        tokenManager.saveTokens(
                                            accessToken = res.accessToken,
                                            userId = res.userId
                                        )
                                        val user = ApiClient.apiService.getUserInfo()
                                        if (user.groupId != null && user.groupId != 0) {
                                            goToMain()
                                            finish()
                                        } else {
                                            showSimplePin = false
                                            showNoGroup = true
                                        }
                                    } catch (e: Exception) {
                                        isSimpleLoading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "간편 로그인 실패: ${e.localizedMessage ?: "알 수 없는 오류"}"
                                            )
                                        }
                                    }
                                }
                            },
                            // 테스트 로그인 기능 추가
                            onLoginClick = { email, password ->
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    authViewModel.login(email, password)
                                    quickLoginEmail = email
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("이메일과 비밀번호를 입력해주세요.")
                                    }
                                }
                            },
                            onShowSnackbar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            showTestButtons = true // 또는 BuildConfig.DEBUG
                        )
                    }

                    showLoginScreen -> {
                        LoginScreen(
                            onLoginClick = { id, password ->
                                if (id.isNotBlank() && password.isNotBlank()) {
                                    authViewModel.login(id, password)
                                    quickLoginEmail = id
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("이메일과 비밀번호를 입력해주세요.")
                                    }
                                }
                            },
                            onFindIdPwClick = { /* TODO */ },
                            onSignUpClick = {
                                startActivity(
                                    Intent(this@LoginActivity, SignUpActivity::class.java)
                                )
                            },
                            onSimplePinLoginClick = {
                                showLoginScreen = false
                                showSimplePin = true
                            },
                            snackbarHostState = snackbarHostState,
                            isLoading = (loginResult is NetworkResult.Loading)
                        )
                    }
                }
            }
        }
    }
}