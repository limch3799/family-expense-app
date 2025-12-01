// file: app/src/main/java/com/moaga/app/ui/screens/account/AccountActivity.kt
package com.moaga.app.ui.screens.account

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.moaga.app.MainActivity
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.FinancialConnectRequest
import com.moaga.app.data.api.dto.response.LinkableAccountsResponse
import com.moaga.app.ui.screens.group.create.AccountLinkPromptScreen
import com.moaga.app.ui.screens.group.create.AccountSelectItemsScreen
import com.moaga.app.ui.screens.group.create.SpendingFetchingScreen
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.launch

class AccountActivity : ComponentActivity() {

    private fun goHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoagaTheme {
                // 🔹 상태 관리
                var step by rememberSaveable { mutableStateOf("prompt") }
                var accountsAndCards by remember { mutableStateOf<LinkableAccountsResponse?>(null) }
                val scope = rememberCoroutineScope()

                when (step) {
                    // 1) 계좌/카드 불러오기 프롬프트
                    "prompt" -> AccountLinkPromptScreen(
                        onContinue = {
                            scope.launch {
                                try {
                                    val res = ApiClient.apiService.getLinkableAccounts()
                                    accountsAndCards = res
                                    step = "select"
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@AccountActivity,
                                        "불러오기 실패: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onSkip = { goHome() }
                    )

                    // 2) 계좌/카드 선택 화면
                    "select" -> AccountSelectItemsScreen(
                        accounts = accountsAndCards?.accounts ?: emptyList(),
                        cards = accountsAndCards?.cards ?: emptyList(),
                        onDone = { selectedAccs, selectedCards ->
                            scope.launch {
                                try {
                                    val user = ApiClient.apiService.getUserInfo()
                                    val gid = user.groupId ?: return@launch

                                    // ✅ 선택된 계좌/카드 서버에 반영
                                    ApiClient.apiService.connectFinancial(
                                        FinancialConnectRequest(
                                            groupId = gid.toLong(),
                                            accountIds = selectedAccs,
                                            cardIds = selectedCards
                                        )
                                    )

                                    step = "fetching"
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@AccountActivity,
                                        "연결 실패: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    // 3) 불러오는 중 → 3초 후 홈으로 이동
                    "fetching" -> SpendingFetchingScreen(
                        onDone = { goHome() }
                    )
                }
            }
        }
    }
}
