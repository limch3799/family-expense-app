// file: app/src/main/java/com/moaga/app/ui/screens/group/GroupActivity.kt
package com.moaga.app.ui.screens.group

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moaga.app.MainActivity
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.IdResponse
import com.moaga.app.data.api.dto.request.GroupCodeRequest
import com.moaga.app.data.api.dto.response.GroupResponse
import com.moaga.app.ui.screens.account.AccountActivity
import com.moaga.app.ui.screens.group.create.*
import com.moaga.app.ui.screens.verify.PhoneVerificationScreen
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.launch

// Join flow
private const val ROUTE_JOIN_INPUT  = "join_input"
private const val ROUTE_JOIN_RESULT = "join_result?inviteCode={inviteCode}&name={name}&desc={desc}&owner={owner}"
private const val ROUTE_JOIN_DONE   = "join_done"

// Create flow
private const val ROUTE_CREATE_INPUT        = "create_input"
private const val ROUTE_CREATE_TERMS        = "create_terms"
private const val ROUTE_CREATE_CAMERA       = "create_camera"
private const val ROUTE_CREATE_LOADING      = "create_loading"
private const val ROUTE_CREATE_ID_CHECK     = "create_id_check?name={name}&rrn={rrn}&issue={issue}"
private const val ROUTE_CREATE_PHONE_VERIFY = "create_phone_verify"
private const val ROUTE_CREATE_ACCOUNT_APPLY= "create_account_apply"
private const val ROUTE_CREATE_OPENING      = "create_opening"
private const val ROUTE_CREATE_OPENED       = "create_opened"

class GroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startFlow = intent.getStringExtra("flow") ?: "join"

        setContent {
            MoagaTheme {
                GroupNavHost(
                    startFlow = startFlow,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
private fun GroupNavHost(
    startFlow: String,
    onFinish: () -> Unit
) {
    val nav = rememberNavController()
    val startDest = if (startFlow == "create") ROUTE_CREATE_INPUT else ROUTE_JOIN_INPUT

    NavHost(navController = nav, startDestination = startDest) {

        // ---------------------- JOIN FLOW ----------------------
        composable(ROUTE_JOIN_INPUT) {
            val scope = rememberCoroutineScope()
            val ctx = LocalContext.current

            GroupJoinScreen(
                onBack = onFinish,
                onSearch = { code ->
                    scope.launch {
                        try {
                            val res = ApiClient.apiService.getGroupInfo(GroupCodeRequest(code))
                            if (res.isSuccessful) {
                                val body: GroupResponse? = res.body()
                                if (body != null) {
                                    nav.navigate(
                                        "join_result" +
                                                "?inviteCode=${Uri.encode(code)}" +
                                                "&name=${Uri.encode(body.name)}" +
                                                "&desc=${Uri.encode(body.description)}" +
                                                "&owner=${Uri.encode(body.ownerName)}"
                                    )
                                } else {
                                    Toast.makeText(ctx, "그룹 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(ctx, "그룹 검색 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(
            route = ROUTE_JOIN_RESULT,
            arguments = listOf(
                navArgument("inviteCode"){ type = NavType.StringType },
                navArgument("name")      { type = NavType.StringType },
                navArgument("desc")      { type = NavType.StringType },
                navArgument("owner")     { type = NavType.StringType },
            )
        ) { backStack ->
            GroupInfoScreen(
                inviteCode  = backStack.arguments?.getString("inviteCode").orEmpty(),
                name        = backStack.arguments?.getString("name").orEmpty(),
                description = backStack.arguments?.getString("desc").orEmpty(),
                ownerName   = backStack.arguments?.getString("owner").orEmpty(),
                onBack = { nav.popBackStack() },
                onJoined = {
                    nav.navigate(ROUTE_JOIN_DONE) {
                        popUpTo(ROUTE_JOIN_INPUT) { inclusive = false }
                    }
                }
            )
        }

        composable(ROUTE_JOIN_DONE) {
            val ctx = LocalContext.current
            GroupJoinCompleteScreen(
                onGoHome = {
                    ctx.startActivity(Intent(ctx, AccountActivity::class.java))
                    onFinish()
                }
            )
        }

        // ---------------------- CREATE FLOW ----------------------
        composable(ROUTE_CREATE_INPUT) {
            GroupCreateScreen(
                onBack = onFinish,
                onNext = { _, _ -> nav.navigate(ROUTE_CREATE_TERMS) }
            )
        }

        composable(ROUTE_CREATE_TERMS) {
            GroupTermsScreen(
                onBack = { nav.popBackStack() },
                onAgree = { nav.navigate(ROUTE_CREATE_CAMERA) }
            )
        }

        // ✅ 카메라 → OCR 촬영
        composable(ROUTE_CREATE_CAMERA) {
            IdCameraScreen(
                onBack = { nav.popBackStack() },
                onCaptured = { uri, response ->
                    nav.currentBackStackEntry?.savedStateHandle?.apply {
                        set("capturedUri", uri)
                        set("idResponse", response)
                    }
                    nav.navigate(ROUTE_CREATE_LOADING)
                }
            )
        }

        // ✅ 로딩 화면
        composable(ROUTE_CREATE_LOADING) {
            val savedState = nav.previousBackStackEntry?.savedStateHandle
            val uri = savedState?.get<Uri>("capturedUri") ?: Uri.EMPTY
            val response = savedState?.get<IdResponse>("idResponse")

            IdCheckingScreen(
                uri = uri,
                response = response
            ) { res ->
                if (res?.success == true && res.data != null) {
                    // ✅ OCR 성공 → 실제 값 사용
                    val name = Uri.encode(res.data.name)
                    val rrn = Uri.encode("${res.data.id_number_front}-${res.data.id_number_back_first}******")
                    val issue = Uri.encode(res.data.issue_date)
                    nav.navigate("create_id_check?name=$name&rrn=$rrn&issue=$issue") {
                        popUpTo(ROUTE_CREATE_CAMERA) { inclusive = false }
                    }
                } else {
                    // ✅ OCR 실패 → 디폴트 값으로 이동
                    val defaultName = Uri.encode("홍길동")
                    val defaultRrn = Uri.encode("000000-0******")
                    val defaultIssue = Uri.encode("2000.01.01")
                    nav.navigate("create_id_check?name=$defaultName&rrn=$defaultRrn&issue=$defaultIssue") {
                        popUpTo(ROUTE_CREATE_CAMERA) { inclusive = false }
                    }
                }
            }
        }

        composable(
            ROUTE_CREATE_ID_CHECK,
            arguments = listOf(
                navArgument("name"){ type = NavType.StringType },
                navArgument("rrn"){ type = NavType.StringType },
                navArgument("issue"){ type = NavType.StringType },
            )
        ) { back ->
            IdInfoConfirmScreen(
                name  = back.arguments?.getString("name").orEmpty(),
                rrn   = back.arguments?.getString("rrn").orEmpty(),
                issue = back.arguments?.getString("issue").orEmpty(),
                onBack = { nav.popBackStack() },
                onYes  = { nav.navigate(ROUTE_CREATE_PHONE_VERIFY) },
                onNo   = { nav.popBackStack(route = ROUTE_CREATE_CAMERA, inclusive = false) }
            )
        }

        composable(ROUTE_CREATE_PHONE_VERIFY) {
            PhoneVerificationScreen(
                onBack = { nav.popBackStack() },
                onSendSmsCode = { _ -> /* TODO */ },
                onVerifySmsCode = { _, _ -> /* TODO */ },
                onNext = { _ -> nav.navigate(ROUTE_CREATE_ACCOUNT_APPLY) }
            )
        }

        composable(ROUTE_CREATE_ACCOUNT_APPLY) {
            AccountApplyScreen(
                onBack = { nav.popBackStack() },
                onDone = { _, _ -> nav.navigate(ROUTE_CREATE_OPENING) }
            )
        }

        composable(ROUTE_CREATE_OPENING) {
            AccountOpeningScreen(
                onDone = {
                    nav.navigate(ROUTE_CREATE_OPENED) {
                        popUpTo(ROUTE_CREATE_ACCOUNT_APPLY) { inclusive = false }
                    }
                }
            )
        }

        composable(ROUTE_CREATE_OPENED) {
            val ctx = LocalContext.current
            AccountOpenedScreen(
                onYes = { ctx.startActivity(Intent(ctx, AccountActivity::class.java)) },
                onLater = {
                    ctx.startActivity(
                        Intent(ctx, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            )
        }
    }
}
