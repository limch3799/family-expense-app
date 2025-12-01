// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanCreateScreen.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// 메인 그린
private val PrimaryGreen = Color(0xFF18A87E)
// 목록 배경: 메인그린에서 아주 연하게 뺀 톤
private val ListBg       = Color(0xFFEAF6F2)   // 연한 민트 (18A87E 기반)
private val SectionBg    = Color(0xFFDFF1EA)   // 섹션 박스도 살짝 녹색 톤
private val CardBg       = Color.White

data class SavingProduct(
    val uniqueNo: String,
    val bankName: String,
    val accountName: String,
    val description: String,
    val periodDays: Int,      // 30 / 90 / 180 / 365 ...
    val minBalance: Int,
    val maxBalance: Int,
    val interestRate: Float,  // 5.0 등
    val rateDesc: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreateScreen(
    products: List<SavingProduct>,
    onBack: () -> Unit = {},
    onSelect: (SavingProduct) -> Unit = {}
) {
    val short = remember(products) { products.filter { it.periodDays <= 90 } }
    val mid   = remember(products) { products.filter { it.periodDays in 91..364 } }
    val long  = remember(products) { products.filter { it.periodDays >= 365 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth().padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("플랜 생성", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ListBg)
                .padding(inner)
                .verticalScroll(rememberScrollState())   // ✅ 스크롤 가능
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("가입 가능 상품 목록", color = Color(0xFF1A1D1F), fontSize = 14.sp)

            SectionBox(title = "단기 플랜") {
                if (short.isEmpty()) EmptyHint("단기 상품이 없습니다.")
                short.forEach { p ->
                    ProductItemCard(product = p, onClick = { onSelect(p) })
                }
            }

            SectionBox(title = "중기 플랜") {
                if (mid.isEmpty()) EmptyHint("중기 상품이 없습니다.")
                mid.forEach { p ->
                    ProductItemCard(product = p, onClick = { onSelect(p) })
                }
            }

            SectionBox(title = "장기 플랜") {
                if (long.isEmpty()) EmptyHint("장기 상품이 없습니다.")
                long.forEach { p ->
                    ProductItemCard(product = p, onClick = { onSelect(p) })
                }
            }

            Spacer(Modifier.height(24.dp)) // ✅ 끝 여백
        }
    }
}


@Composable
fun EmptyHint(message: String) {
    Text(
        text = message,
        color = Color(0xFF6C7682),
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
private fun SectionBox(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            title,
            fontSize = 14.sp,
            color = Color(0xFF2F3A34),
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun ProductItemCard(
    product: SavingProduct,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .clickable(onClick = onClick),
        color = CardBg,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1) 적금 이름
            Text(
                text = product.accountName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )

            Spacer(Modifier.height(4.dp))

            // 2) 설명
            Text(
                text = product.description,
                fontSize = 13.sp,
                color = Color(0xFF6C7682)
            )

            Spacer(Modifier.height(8.dp))

            // 3) 금액 (최소 ~ 최대)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "기간: ${daysToLabel(product.periodDays)} · 금리: ${trimRate(product.interestRate)}%",
                    fontSize = 12.sp,
                    color = Color(0xFF87919C)
                )
                Text(
                    text = "${formatWon(product.minBalance)} ~ ${formatWon(product.maxBalance)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen
                )
            }
        }
    }
}

/* ---------- 유틸 ---------- */

private fun formatWon(amount: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale.KOREA)
    return nf.format(amount) + "원"
}

private fun daysToLabel(days: Int): String = when (days) {
    30 -> "1개월"
    90 -> "3개월"
    180 -> "6개월"
    365 -> "1년"
    else -> "${days}일"
}

private fun trimRate(r: Float): String =
    if (r % 1f == 0f) r.toInt().toString() else r.toString()

/* ---------- 샘플 데이터 ---------- */

fun sampleProducts(): List<SavingProduct> = listOf(
    SavingProduct(
        uniqueNo = "999-3-2d6e8ce340664f",
        bankName = "싸피은행",
        accountName = "한달 플러스 적금",
        description = "1개월 만기, 짧게 운용하는 초단기 적금",
        periodDays = 30,
        minBalance = 10_000, maxBalance = 200_000,
        interestRate = 5.0f,
        rateDesc = "30일 만기, 연 5% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "001-3-6e0cbefff44e43",
        bankName = "한국은행",
        accountName = "1개월 스마트 적금",
        description = "소액도 가능, 1개월 단기 적금",
        periodDays = 30,
        minBalance = 50_000, maxBalance = 1_000_000,
        interestRate = 4.5f,
        rateDesc = "30일 만기, 연 4.5% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "999-3-3af80b0f43a54a",
        bankName = "싸피은행",
        accountName = "3개월 자유 적금",
        description = "자유롭게 납입 가능한 3개월 적금",
        periodDays = 90,
        minBalance = 50_000, maxBalance = 200_000,
        interestRate = 3.8f,
        rateDesc = "90일 만기, 연 3.8% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "999-3-2b20dab33d0544",
        bankName = "싸피은행",
        accountName = "반년 베이직 적금",
        description = "6개월 만기 기본형 적금 상품",
        periodDays = 180,
        minBalance = 200_000, maxBalance = 500_000,
        interestRate = 3.5f,
        rateDesc = "180일 만기, 연 3.5% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "999-3-57fcadbaa5db41",
        bankName = "싸피은행",
        accountName = "6개월 하이 적금",
        description = "중기 자금 마련에 유리한 6개월 적금",
        periodDays = 180,
        minBalance = 300_000, maxBalance = 700_000,
        interestRate = 3.2f,
        rateDesc = "180일 만기, 연 3.2% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "001-3-5b74e32f805746",
        bankName = "한국은행",
        accountName = "1년 행복 적금",
        description = "안정적인 1년 만기 장기 적금",
        periodDays = 365,
        minBalance = 300_000, maxBalance = 1_000_000,
        interestRate = 3.0f,
        rateDesc = "365일 만기, 연 3% 이자를 지급합니다"
    ),
    SavingProduct(
        uniqueNo = "001-3-a5365b21fa8647",
        bankName = "한국은행",
        accountName = "1년 플러스 적금",
        description = "1년 만기, 우대금리 제공",
        periodDays = 365,
        minBalance = 500_000, maxBalance = 1_000_000,
        interestRate = 3.2f,
        rateDesc = "365일 만기, 연 3.2% 이자를 지급합니다"
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "Plan Create")
@Composable
private fun PlanCreatePreview() {
    PlanCreateScreen(products = sampleProducts())
}
