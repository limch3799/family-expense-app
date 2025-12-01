// file: app/src/main/java/com/moaga/app/ui/screens/account/LinkedAccountsScreen.kt
package com.moaga.app.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import com.airbnb.lottie.compose.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.moaga.app.R
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.FinancialConnectRequest
import com.moaga.app.data.api.dto.request.FinancialDisconnectRequest
import kotlinx.coroutines.launch

// 계좌/카드 아이템 데이터
data class AccountItemData(
    val id: String,        // "acc_123", "card_456"
    val title: String,     // 은행/카드사
    val subtitle: String,  // 계좌/카드번호
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedAccountsScreen(navController: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // 서버에서 가져온 현재 연결 목록
    var linkedAccounts by remember { mutableStateOf<List<AccountItemData>>(emptyList()) }

    // 수정 모드/바텀시트/로딩
    var isEditMode by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // 연결 가능 목록(바텀시트에 표시)
    var availableAccounts by remember { mutableStateOf<List<AccountItemData>>(emptyList()) }

    // 내 그룹 ID (서버에서 조회)
    var groupId by remember { mutableStateOf<Long?>(null) }

    // “수정 완료” 시 서버로 보낼 대기 목록
    val toConnectAccounts = remember { mutableStateListOf<Long>() }
    val toConnectCards    = remember { mutableStateListOf<Long>() }
    // 삭제는 따로 쪼개서 관리(단순)
    val toDisconnectAccs  = remember { mutableStateListOf<Long>() }
    val toDisconnectCards = remember { mutableStateListOf<Long>() }

    val sheetState = rememberModalBottomSheetState()

    // 초기 로딩: 내 정보 → groupId → 현재 연결 목록 조회
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val user = ApiClient.apiService.getUserInfo()
            groupId = user.groupId?.toLong()
            if (groupId == null) {
                Toast.makeText(ctx, "그룹이 없습니다.", Toast.LENGTH_SHORT).show()
                linkedAccounts = emptyList()
            } else {
                val res = ApiClient.apiService.getConnectedAccounts(groupId!!)
                linkedAccounts =
                    res.myConnectedAccounts.map {
                        AccountItemData(
                            id = "acc_${it.accountId}",
                            title = it.bankName,
                            subtitle = "계좌번호: ${it.accountNo}",
                            imageRes = R.drawable.account_sample
                        )
                    } + res.myConnectedCards.map {
                        AccountItemData(
                            id = "card_${it.cardId}",
                            title = it.cardCompany,
                            subtitle = "카드번호: ${it.cardNo}",
                            imageRes = R.drawable.card_sample
                        )
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "연결된 목록 조회 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        // 상단 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Spacer(Modifier.width(8.dp))
            Text("마이데이터 관리", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "  현재 그룹에 연동된 계좌/카드 목록입니다.",
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )

        Spacer(Modifier.height(8.dp))

        // 현재 연결 목록
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(linkedAccounts, key = { it.id }) { account ->
                AccountItem(
                    account = account,
                    showDelete = isEditMode,
                    onDelete = {
                        // UI에서 먼저 제거
                        linkedAccounts = linkedAccounts.filter { it.id != account.id }

                        // 삭제 대기목록에 적재
                        if (account.id.startsWith("acc_")) {
                            val id = account.id.removePrefix("acc_").toLong()
                            // 만약 이번 세션에서 추가로 선택해둔 항목이었다면 connect 대기에서 제거
                            toConnectAccounts.remove(id)
                            if (!toDisconnectAccs.contains(id)) toDisconnectAccs.add(id)
                        } else if (account.id.startsWith("card_")) {
                            val id = account.id.removePrefix("card_").toLong()
                            toConnectCards.remove(id)
                            if (!toDisconnectCards.contains(id)) toDisconnectCards.add(id)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 수정 모드에서만: “불러오기”
        if (isEditMode) {
            Button(
                onClick = {
                    scope.launch {
                        showBottomSheet = true
                        isLoading = true
                        try {
                            val res = ApiClient.apiService.getLinkableAccounts()
                            // 이미 연결된 것/이번 세션에서 삭제 대기 중인 것 제외하고 구성
                            val alreadyIds = linkedAccounts.map { it.id }.toSet()

                            availableAccounts =
                                res.accounts
                                    .filter { !it.isConnectedToGroup } // 이미 그룹에 연결된 건 제외
                                    .map {
                                        AccountItemData(
                                            id = "acc_${it.accountId}",
                                            title = it.bankName,
                                            subtitle = "계좌번호: ${it.accountNo}",
                                            imageRes = R.drawable.account_sample
                                        )
                                    }.filter { it.id !in alreadyIds } +
                                        res.cards
                                            .filter { !it.isConnectedToGroup }
                                            .map {
                                                AccountItemData(
                                                    id = "card_${it.cardId}",
                                                    title = it.cardCompany,
                                                    subtitle = "카드번호: ${it.cardNo}",
                                                    imageRes = R.drawable.card_sample
                                                )
                                            }.filter { it.id !in alreadyIds }

                            showBottomSheet = true
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) { Text("카드 / 계좌내역 불러오기") }

            Spacer(Modifier.height(16.dp))
        }

        // 하단: 수정 ↔ 수정 완료
        Button(
            onClick = {
                if (!isEditMode) {
                    // 수정 모드 진입
                    isEditMode = true
                } else {
                    // 수정 완료 → 서버 반영
                    if (groupId == null) {
                        Toast.makeText(ctx, "그룹이 없어 반영할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        try {
                            // 1) Connect(한 번에)
                            if (toConnectAccounts.isNotEmpty() || toConnectCards.isNotEmpty()) {
                                ApiClient.apiService.connectFinancial(
                                    FinancialConnectRequest(
                                        groupId = groupId!!,
                                        accountIds = toConnectAccounts.toList(),
                                        cardIds = toConnectCards.toList()
                                    )
                                )
                            }
                            // 2) Disconnect(개별)
                            toDisconnectAccs.forEach { accId ->
                                ApiClient.apiService.disconnectFinancial(
                                    FinancialDisconnectRequest(
                                        groupId = groupId!!,
                                        accountId = accId,
                                        cardId = null
                                    )
                                )
                            }
                            toDisconnectCards.forEach { cardId ->
                                ApiClient.apiService.disconnectFinancial(
                                    FinancialDisconnectRequest(
                                        groupId = groupId!!,
                                        accountId = null,
                                        cardId = cardId
                                    )
                                )
                            }

                            // 반영 후 서버에서 최신 연결 목록 재조회
                            val refreshed = ApiClient.apiService.getConnectedAccounts(groupId!!)
                            linkedAccounts =
                                refreshed.myConnectedAccounts.map {
                                    AccountItemData(
                                        id = "acc_${it.accountId}",
                                        title = it.bankName,
                                        subtitle = "계좌번호: ${it.accountNo}",
                                        imageRes = R.drawable.account_sample
                                    )
                                } + refreshed.myConnectedCards.map {
                                    AccountItemData(
                                        id = "card_${it.cardId}",
                                        title = it.cardCompany,
                                        subtitle = "카드번호: ${it.cardNo}",
                                        imageRes = R.drawable.card_sample
                                    )
                                }

                            Toast.makeText(ctx, "연동 상태가 저장되었습니다.", Toast.LENGTH_SHORT).show()

                            // 대기목록/모드 초기화
                            isEditMode = false
                            toConnectAccounts.clear()
                            toConnectCards.clear()
                            toDisconnectAccs.clear()
                            toDisconnectCards.clear()
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "서버 반영 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18A87E))
        ) {
            Text(if (isEditMode) "수정 완료" else "수정")
        }
    }

    // 바텀시트: 연결 가능한 항목들
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    // ✅ Lottie 로딩 애니메이션
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.loading_block)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("불러오는 중...", fontSize = 14.sp, color = Color.Gray)
                } else {
                    // ✅ 계좌/카드 리스트
                    Text("조회된 나의 카드/계좌", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    LazyColumn {
                        items(availableAccounts) { item ->
                            AccountItem(
                                account = item,
                                showCheck = true,
                                onAdd = {
                                    if (linkedAccounts.any { it.id == item.id }) return@AccountItem
                                    linkedAccounts = linkedAccounts + item
                                    if (item.id.startsWith("acc_")) {
                                        val id = item.id.removePrefix("acc_").toLong()
                                        if (!toConnectAccounts.contains(id)) toConnectAccounts.add(id)
                                        toDisconnectAccs.remove(id)
                                    } else if (item.id.startsWith("card_")) {
                                        val id = item.id.removePrefix("card_").toLong()
                                        if (!toConnectCards.contains(id)) toConnectCards.add(id)
                                        toDisconnectCards.remove(id)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18A87E))
                    ) { Text("추가하기") }
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    account: AccountItemData,
    showDelete: Boolean = false,
    showCheck: Boolean = false,
    onDelete: () -> Unit = {},
    onAdd: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽: 로고 + 텍스트
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFEAF6F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = account.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        account.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111111)
                    )
                    Text(
                        account.subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 오른쪽: 삭제 or 체크박스
            when {
                showDelete -> {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFE5E5), CircleShape) // 연한 빨강 배경
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close, // 둥근 Close 아이콘
                            contentDescription = "삭제",
                            tint = Color(0xFFEF4444), // 진한 빨강
                            modifier = Modifier.size(20.dp)   // 아이콘 조금 작게
                        )
                    }
                }
                showCheck -> {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { onAdd() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF18A87E),
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
