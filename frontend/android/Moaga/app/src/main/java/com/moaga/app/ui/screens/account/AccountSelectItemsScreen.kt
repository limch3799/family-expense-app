// file: app/src/main/java/com/moaga/app/ui/screens/group/create/AccountSelectItemsScreen.kt
package com.moaga.app.ui.screens.group.create

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.moaga.app.data.api.dto.response.AccountDto
import com.moaga.app.data.api.dto.response.CardDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LinkableItem(
    val id: String,
    val title: String,
    val subtitle: String,
    var selected: Boolean = false
)

@Composable
fun AccountSelectItemsScreen(
    accounts: List<AccountDto>,
    cards: List<CardDto>,
    onDone: (selectedAccounts: List<Long>, selectedCards: List<Long>) -> Unit,
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo
) {
    val brandBlue = Color(0xFF18A87E)
    val listBg    = Color(0xFFF3F6FB)

    // 샘플 데이터 (이미지 없음)
    val items = remember {
        mutableStateListOf<LinkableItem>()
    }

    // 애니메이션 상태
    var animationStarted by remember { mutableStateOf(false) }

    // 계좌 + 카드 → LinkableItem으로 변환
    LaunchedEffect(accounts, cards) {
        items.clear()
        items.addAll(
            accounts.map {
                LinkableItem(
                    id = "account-${it.accountId}",
                    title = it.bankName,
                    subtitle = "계좌번호: ${it.accountNo}",
                    selected = it.isConnectedToGroup
                )
            }
        )
        items.addAll(
            cards.map {
                LinkableItem(
                    id = "card-${it.cardId}",
                    title = it.cardCompany,
                    subtitle = "카드번호: ${it.cardNo}",
                    selected = it.isConnectedToGroup
                )
            }
        )

        // 데이터가 로드된 후 애니메이션 시작
        delay(200)
        animationStarted = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // --- 헤더(중앙 정렬) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))
            Text(
                "마이데이터 불러오기",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111111),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "연동할 개인정보 항목을 선택해주세요.",
                fontSize = 16.sp,
                color = Color(0xFF2B3240),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- 리스트 카드 영역 ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 남은 공간을 차지하되 버튼을 위한 공간 확보
            shape = RoundedCornerShape(16.dp),
            color = listBg
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 아이템 사이 간격 추가
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    AnimatedListItem(
                        item = item,
                        index = index,
                        animationStarted = animationStarted,
                        brandBlue = brandBlue,
                        onClick = {
                            val idx = items.indexOfFirst { it.id == item.id }
                            if (idx >= 0) items[idx] = items[idx].copy(selected = !items[idx].selected)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // ✅ 선택된 항목 정리
                val selectedAccs = items.filter { it.selected && it.id.startsWith("account-") }
                    .map { it.id.removePrefix("account-").toLong() }
                val selectedCards = items.filter { it.selected && it.id.startsWith("card-") }
                    .map { it.id.removePrefix("card-").toLong() }

                onDone(selectedAccs, selectedCards)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
        ) {
            Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSelectItemsScreenPreview() {
    // 샘플 데이터
    val sampleAccounts = listOf(
        AccountDto(
            accountId = 1L,
            bankName = "싸피은행",
            accountNo = "110-123-456789",
            isConnectedToGroup = true
        ),
        AccountDto(
            accountId = 2L,
            bankName = "국민은행",
            accountNo = "123-456-789012",
            isConnectedToGroup = false
        ),
        AccountDto(
            accountId = 3L,
            bankName = "신한은행",
            accountNo = "789-012-345678",
            isConnectedToGroup = false
        ),
        AccountDto(
            accountId = 2L,
            bankName = "국민은행",
            accountNo = "123-456-789012",
            isConnectedToGroup = false
        ),
        AccountDto(
            accountId = 3L,
            bankName = "신한은행",
            accountNo = "789-012-345678",
            isConnectedToGroup = false
        ),
        AccountDto(
            accountId = 2L,
            bankName = "국민은행",
            accountNo = "123-456-789012",
            isConnectedToGroup = false
        ),
        AccountDto(
            accountId = 3L,
            bankName = "신한은행",
            accountNo = "789-012-345678",
            isConnectedToGroup = false
        )
    )

    val sampleCards = listOf(
        CardDto(
            cardId = 1L,
            cardCompany = "삼성카드",
            cardNo = "1234-****-****-5678",
            isConnectedToGroup = true
        ),
        CardDto(
            cardId = 2L,
            cardCompany = "현대카드",
            cardNo = "9876-****-****-4321",
            isConnectedToGroup = false
        )
    )

    AccountSelectItemsScreen(
        accounts = sampleAccounts,
        cards = sampleCards,
        onDone = { selectedAccounts, selectedCards ->
            // Preview에서는 동작하지 않음
        }
    )
}

@Composable
fun AnimatedListItem(
    item: LinkableItem,
    index: Int,
    animationStarted: Boolean,
    brandBlue: Color,
    onClick: () -> Unit
) {
    // 개별 아이템의 애니메이션 상태
    val translationY = remember { Animatable(80f) } // 더 많이 내려서 시작
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(animationStarted) {
        if (animationStarted) {
            // 인덱스에 따라 지연시간 증가 (더 느리게)
            delay(index * 150L) // 50ms -> 150ms로 변경

            // 병렬로 애니메이션 실행
            launch {
                translationY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow // 더 느리게 변경
                    )
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow // 더 느리게 변경
                    )
                )
            }
        }
    }

    // 카드 스타일 배경 추가
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.translationY = translationY.value
            }
            .alpha(alpha.value)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp), // 패딩 증가
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111)
                )
                Spacer(Modifier.height(4.dp)) // 간격 증가
                Text(item.subtitle, fontSize = 13.sp, color = Color(0xFF7B8190))
            }
            Spacer(Modifier.width(12.dp)) // 간격 증가
            if (item.selected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "selected",
                    tint = brandBlue,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "unselected",
                    tint = Color(0xFFB9BDC6),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}