// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanActivity.kt
package com.moaga.app.ui.screens.plan

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moaga.app.ui.screens.group.create.AccountOpeningScreen
import com.moaga.app.ui.screens.group.create.IdCameraScreen
import com.moaga.app.ui.screens.group.create.IdCheckingScreen
import com.moaga.app.ui.screens.group.create.IdInfoConfirmScreen
import com.moaga.app.ui.screens.verify.PhoneVerificationScreen
import com.moaga.app.ui.theme.MoagaTheme
import com.moaga.app.data.api.IdResponse   // ✅ OCR 응답 모델
// 필요한 다른 import들(PlanCreateScreen, SavingTermsScreen, PlanApplyScreen 등)은 기존과 동일하게 유지

/** 라우트: 생성 플로우 + 상세 + 지난 플랜 목록 */
private object PlanRoutes {
    const val CREATE       = "plan_create"
    const val TERMS        = "plan_terms"
    const val CAMERA       = "plan_id_camera"
    const val LOADING      = "plan_id_loading"
    const val ID_CHECK     = "plan_id_check?name={name}&rrn={rrn}&issue={issue}"
    const val PHONE_VERIFY = "plan_phone_verify"
    const val APPLY        = "plan_apply"
    const val OPENING      = "plan_opening"
    const val OPENED       = "plan_opened_done"
    const val DETAIL       = "plan_detail"
    const val HISTORY_LIST = "plan_history"
}

class PlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialHasPlan = intent?.getBooleanExtra("hasPlan", false) ?: false
        val startHistory   = intent?.getBooleanExtra("startHistory", false) ?: false

        setContent {
            MoagaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlanNavHost(
                        initialHasPlan = initialHasPlan,
                        startHistory = startHistory,
                        onExit = { finish() }
                    )
                }
            }
        }
    }
}

/** Plan: 플랜 유무/의도에 따라 시작 분기 + 생성 플로우 + 상세/지난목록 */
@Composable
fun PlanNavHost(
    initialHasPlan: Boolean = false,
    startHistory: Boolean = false,
    onExit: () -> Unit,
    nav: NavHostController = rememberNavController(),
) {
    var hasPlan by rememberSaveable { mutableStateOf(initialHasPlan) }

    val startDest = remember(initialHasPlan, startHistory) {
        when {
            startHistory   -> PlanRoutes.HISTORY_LIST
            initialHasPlan -> PlanRoutes.DETAIL
            else           -> PlanRoutes.CREATE
        }
    }

    NavHost(
        navController = nav,
        startDestination = startDest
    ) {
        // 1) 상품 목록
        composable(PlanRoutes.CREATE) {
            PlanCreateScreen(
                products = sampleProducts(),
                onBack = { onExit() },
                onSelect = { _ -> nav.navigate(PlanRoutes.TERMS) }
            )
        }

        // 2) 약관 동의
        composable(PlanRoutes.TERMS) {
            SavingTermsScreen(
                onBack = { nav.popBackStack() },
                onAgree = { nav.navigate(PlanRoutes.CAMERA) }
            )
        }

        // 3) 신분증 촬영 + OCR(카메라 화면에서 처리하여 결과 콜백으로 전달)
        composable(PlanRoutes.CAMERA) {
            IdCameraScreen(
                onBack = { nav.popBackStack() },
                onCaptured = { uri: Uri, idRes: IdResponse? ->
                    // 촬영/업로드 완료 후: Uri와 OCR 응답을 저장
                    nav.currentBackStackEntry?.savedStateHandle?.apply {
                        set("capturedUri", uri)
                        set("idResponse", idRes)
                    }
                    // 로딩 화면으로 이동
                    nav.navigate(PlanRoutes.LOADING)
                }
            )
        }

        // 4) 판독 로딩 → 결과에 따라 다음 화면으로
        composable(PlanRoutes.LOADING) {
            val ctx = LocalContext.current
            // CAMERA에서 저장한 상태 꺼내기
            val savedState = nav.previousBackStackEntry?.savedStateHandle
            val capturedUri = savedState?.get<Uri>("capturedUri") ?: Uri.EMPTY
            val idResponse  = savedState?.get<IdResponse>("idResponse")

            IdCheckingScreen(
                uri = capturedUri,
                response = idResponse,
                onDone = { res ->
                    if (res?.success == true && res.data != null) {
                        val name  = Uri.encode(res.data.name)
                        val rrn   = Uri.encode("${res.data.id_number_front} - ${res.data.id_number_back_first}******")
                        val issue = Uri.encode(res.data.issue_date)
                        nav.navigate("plan_id_check?name=$name&rrn=$rrn&issue=$issue") {
                            // 카메라 스택은 남겨두고 뒤로가면 카메라로 돌아가게
                            popUpTo(PlanRoutes.CAMERA) { inclusive = false }
                        }
                    } else {
                        Toast.makeText(
                            ctx,
                            "신분증 인식 실패: ${res?.message ?: "다시 촬영해주세요."}",
                            Toast.LENGTH_SHORT
                        ).show()
                        nav.popBackStack(PlanRoutes.CAMERA, inclusive = false)
                    }
                }
            )
        }

        // 5) 정보 확인
        composable(
            route = PlanRoutes.ID_CHECK,
            arguments = listOf(
                navArgument("name")  { type = NavType.StringType },
                navArgument("rrn")   { type = NavType.StringType },
                navArgument("issue") { type = NavType.StringType },
            )
        ) { back ->
            IdInfoConfirmScreen(
                name  = back.arguments?.getString("name").orEmpty(),
                rrn   = back.arguments?.getString("rrn").orEmpty(),
                issue = back.arguments?.getString("issue").orEmpty(),
                onBack = { nav.popBackStack() },
                onYes  = { nav.navigate(PlanRoutes.PHONE_VERIFY) },
                onNo   = { nav.popBackStack(route = PlanRoutes.CAMERA, inclusive = false) }
            )
        }

        // 6) 휴대폰 인증
        composable(PlanRoutes.PHONE_VERIFY) {
            PhoneVerificationScreen(
                onBack = { nav.popBackStack() },
                onSendSmsCode = { _ -> /* TODO 서버요청 */ },
                onVerifySmsCode = { _, _ -> /* TODO 검증 */ },
                onNext = { _ -> nav.navigate(PlanRoutes.APPLY) }
            )
        }

        // 7) 개설 신청
        composable(PlanRoutes.APPLY) {
            PlanApplyScreen(
                onBack = { nav.popBackStack() },
                onComplete = { nav.navigate(PlanRoutes.OPENING) }
            )
        }

        // 8) 개설 중
        composable(PlanRoutes.OPENING) {
            AccountOpeningScreen(
                onDone = {
                    nav.navigate(PlanRoutes.OPENED) {
                        popUpTo(PlanRoutes.APPLY) { inclusive = false }
                    }
                }
            )
        }

        // 9) 개설 완료 → 상세로
        composable(PlanRoutes.OPENED) {
            PlanOpenedDoneScreen(
                onDone = {
                    hasPlan = true
                    nav.navigate(PlanRoutes.DETAIL) {
                        popUpTo(PlanRoutes.CREATE) { inclusive = true }
                    }
                }
            )
        }

        // A) 플랜 상세
        composable(PlanRoutes.DETAIL) {
            PlanDetailScreen(
                onBack = { onExit() },
                onClosed = {
                    hasPlan = false
                    nav.navigate(PlanRoutes.CREATE) {
                        popUpTo(PlanRoutes.DETAIL) { inclusive = true }
                    }
                }
            )
        }

        /* // B) 지난 플랜 목록
        composable(PlanRoutes.HISTORY_LIST) {
            PastPlansScreen(
                onBack = { nav.popBackStack() }
            )
        } */
    }
}

@Preview(showSystemUi = true, name = "Plan Flow (hasPlan=false → CREATE)")
@Composable
private fun PlanNavHostPreview_Create() {
    MoagaTheme { PlanNavHost(initialHasPlan = false, onExit = {}) }
}

@Preview(showSystemUi = true, name = "Plan Flow (hasPlan=true → DETAIL)")
@Composable
private fun PlanNavHostPreview_Detail() {
    MoagaTheme { PlanNavHost(initialHasPlan = true, onExit = {}) }
}
