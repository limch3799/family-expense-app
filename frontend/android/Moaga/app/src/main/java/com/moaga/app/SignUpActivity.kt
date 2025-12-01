// file: app/src/main/java/com/d105/app/ui/screens/signup/SignUpActivity.kt
package com.moaga.app.ui.screens.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moaga.app.MainActivity
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.EmailRequest
import com.moaga.app.data.api.dto.request.SignUpRequest
import com.moaga.app.ui.screens.group.GroupActivity
import com.moaga.app.ui.screens.login.LoginActivity
import com.moaga.app.ui.screens.pin.SimplePinSetupScreen
import com.moaga.app.ui.screens.verify.PhoneVerificationScreen
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.launch

private const val ROUTE_SIGNUP_FORM  = "signup_form"
private const val ROUTE_PHONE_VERIFY = "phone_verify"
private const val ROUTE_PIN_SETUP    = "pin_setup"
private const val ROUTE_SIGNUP_DONE  = "signup_done"

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoagaTheme {
                val goMain: () -> Unit = {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
                SignUpNavHost(
                    onExit = { finish() },
                    onFinishToMain = goMain
                )
            }
        }
    }
}

@Composable
private fun SignUpNavHost(
    onExit: () -> Unit,
    onFinishToMain: () -> Unit,
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    // 🔹 단계별 데이터 저장
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var simplePassword by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("M") } // TODO: UI로 선택 가능하게 확장

    NavHost(navController = nav, startDestination = ROUTE_SIGNUP_FORM) {

        // 1. 회원가입 기본정보 입력
        composable(ROUTE_SIGNUP_FORM) {
            var emailCodeMsg by remember { mutableStateOf("") }

            SignUpScreen(
                onBack = onExit,
                onSendEmailCode = { emailInput ->
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.sendEmailCode(
                                EmailRequest.SendEmailRequest(emailInput)
                            )
                            emailCodeMsg = if (res.isSuccessful) {
                                "인증 코드가 전송되었습니다."
                            } else {
                                "전송 실패 (${res.code()})"
                            }
                        } catch (e: Exception) {
                            emailCodeMsg = "네트워크 오류"
                        }
                    }
                },
                onVerifyEmailCode = { emailInput, code ->
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.verifyEmailCode(
                                EmailRequest.VerifyEmailRequest(emailInput, code)
                            )
                            emailCodeMsg = if (res.isSuccessful) {
                                "인증 완료"
                            } else {
                                "인증 실패 (${res.code()})"
                            }
                        } catch (e: Exception) {
                            emailCodeMsg = "네트워크 오류"
                        }
                    }
                },
                onNext = { e, p, n, b ->
                    email = e
                    password = p
                    username = n
                    birthDate = b
                    nav.navigate(ROUTE_PHONE_VERIFY)
                },
                emailCodeMsg = emailCodeMsg
            )
        }

        // 2. 전화번호 인증
        composable(ROUTE_PHONE_VERIFY) {
            var smsMsg by remember { mutableStateOf("") }

            PhoneVerificationScreen(
                onBack = { nav.popBackStack() },
                onSendSmsCode = { phone ->
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.sendSmsCode(
                                com.moaga.app.data.api.dto.request.SmsSendRequest(phone)
                            )
                            smsMsg = if (res.isSuccessful) {
                                "인증 코드가 전송되었습니다."
                            } else {
                                "전송 실패 (${res.code()})"
                            }
                        } catch (e: Exception) {
                            smsMsg = "네트워크 오류"
                        }
                    }
                },
                onVerifySmsCode = { phone, code ->
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.verifySmsCode(
                                com.moaga.app.data.api.dto.request.SmsVerifyRequest(phone, code)
                            )
                            smsMsg = if (res.isSuccessful) {
                                "인증 완료"
                            } else {
                                "인증 실패 (${res.code()})"
                            }
                        } catch (e: Exception) {
                            smsMsg = "네트워크 오류"
                        }
                    }
                },
                onNext = { phone ->
                    phoneNumber = phone
                    nav.navigate(ROUTE_PIN_SETUP)
                },
                smsMsg = smsMsg   // ✅ PhoneVerificationScreen 에 메시지 띄우기
            )
        }

        // 3. 간편비밀번호 설정 + 회원가입 API 호출
        composable(ROUTE_PIN_SETUP) {
            SimplePinSetupScreen(
                onBack = { nav.popBackStack() },
                onComplete = { pin ->
                    simplePassword = pin

                    // ✅ 회원가입 API 호출
                    scope.launch {
                        try {
                            ApiClient.getTokenManager().clearToken()
                            val res = ApiClient.apiService.signUp(
                                SignUpRequest(
                                    email = email,
                                    password = password,
                                    simplePassword = simplePassword,
                                    username = username,
                                    phoneNumber = phoneNumber,
                                    birthDate = birthDate,
                                    gender = gender
                                )
                            )
                            if (res.isSuccessful) {
                                try {
                                    val loginRes = ApiClient.apiService.login(
                                        com.moaga.app.data.api.dto.request.LoginRequest(
                                            email = email,
                                            password = password
                                        )
                                    )

                                    // ✅ 토큰 저장
                                    ApiClient.getTokenManager().saveTokens(
                                        accessToken = loginRes.accessToken,
                                        userId = loginRes.userId
                                    )

                                    // ✅ 간편 로그인 정보도 저장
                                    ApiClient.getTokenManager().setQuickLoginEnabled(true)
                                    ApiClient.getTokenManager().setQuickLoginEmail(email)

                                    // ✅ 메인 화면 or 가입 완료 화면 이동
                                    nav.navigate(ROUTE_SIGNUP_DONE) {
                                        popUpTo(ROUTE_SIGNUP_FORM) { inclusive = false }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        ctx,
                                        "자동 로그인 실패: ${e.localizedMessage ?: "알 수 없는 오류"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                val msg = "회원가입 실패: ${res.code()}"
                                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                            }
                            nav.navigate(ROUTE_SIGNUP_DONE) {
                                popUpTo(ROUTE_SIGNUP_FORM) { inclusive = false }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                ctx,
                                "네트워크 오류: ${e.localizedMessage ?: "알 수 없는 오류"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        // 4. 가입 완료 화면
        composable(ROUTE_SIGNUP_DONE) {
            SignUpCompleteScreen(
                name = username.ifBlank { "고객" },
                onGoToLogin = {
                    ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                }
            )
        }
    }
}
