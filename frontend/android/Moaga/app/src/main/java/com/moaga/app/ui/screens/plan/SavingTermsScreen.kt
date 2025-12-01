// file: app/src/main/java/com/moaga/app/ui/screens/plan/SavingTermsScreen.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 적금 전용 약관 동의 화면 (녹색 계열 적용)
 * - 텍스트/타이틀 외부 주입 없음(하드코딩)
 * - onBack, onAgree 콜백만 연결해서 사용
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingTermsScreen(
    onBack: () -> Unit = {},
    onAgree: () -> Unit = {}
) {
    // ✅ 프로젝트 전반에서 사용하는 녹색 톤과 맞춤
    val PrimaryGreen = Color(0xFF18A87E)  // 메인 그린
    val ListBg       = Color(0xFFEAF6F2)  // 연한 녹색 배경
    val SectionBg    = Color(0xFFDFF1EA)  // 섹션/박스 배경
    val TermsBg    = Color(0xFFF2F2F2)

    val title     = "적금 약관 동의"
    val termsBody = """
        [적금 상품 서비스 이용약관 (요약)]
        1. 목적: 본 약관은 당사 적금 상품의 가입, 이용, 해지 등에 관한 기본 사항을 규정합니다.
        2. 가입대상: 본인 확인 및 전자금융거래 이용 동의가 완료된 고객에 한해 가입할 수 있습니다.
        3. 납입 및 한도: 상품설명서의 납입 주기·방식·최소/최대 한도를 따릅니다.
        4. 이자: 약정 금리는 세전 기준이며, 세율·우대조건·변동 여부는 별도 고지 기준을 따릅니다.
        5. 해지: 만기 전 중도해지 시 중도해지이율이 적용될 수 있으며, 지급 이자는 상품 기준에 따라 감액될 수 있습니다.
        6. 개인정보 및 금융거래정보: 관련 법령과 내부 정책에 따라 수집·이용·제공되며, 상세는 개인정보 처리방침을 따릅니다.
        7. 분쟁처리: 분쟁 발생 시 본 약관, 상품설명서 및 관계 법령을 적용하며, 분쟁조정기구를 통한 조정을 신청할 수 있습니다.
        8. 기타: 본 약관은 사전 고지 후 변경될 수 있으며, 변경 시 서비스 내 공지사항으로 안내합니다.
    """.trimIndent()

    var agree1 by remember { mutableStateOf(false) }
    var agree2 by remember { mutableStateOf(false) }
    var agree3 by remember { mutableStateOf(false) }
    val allChecked = agree1 && agree2 && agree3

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth().padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color.Black
                        )
                    }
                },
                // 상단바 배경은 화이트 유지 (가독성)
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TermsBg) // ✅ 화면 전체 연한 녹색 배경으로 톤 맞춤
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("서비스 이용약관 동의", fontSize = 14.sp, color = Color(0xFF1A1D1F))

            // 약관 본문(스크롤 가능)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)) // ✅ 섹션 박스도 녹색 톤
                    .padding(12.dp)
            ) {
                val scroll = rememberScrollState()
                Text(
                    text = termsBody,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.verticalScroll(scroll)
                )
            }

            Text("필수 동의 항목", fontSize = 14.sp, color = Color(0xFF1A1D1F))

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                AgreeRow(
                    text = "적금 상품 이용약관 동의 (필수)",
                    checked = agree1,
                    onChecked = { agree1 = it },
                    primary = PrimaryGreen
                )
                AgreeRow(
                    text = "개인정보 수집 및 이용 동의 (필수)",
                    checked = agree2,
                    onChecked = { agree2 = it },
                    primary = PrimaryGreen
                )
                AgreeRow(
                    text = "금융거래정보 제공 동의 (필수)",
                    checked = agree3,
                    onChecked = { agree3 = it },
                    primary = PrimaryGreen
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAgree,
                enabled = allChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen) // ✅ 버튼도 녹색
            ) {
                Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AgreeRow(
    text: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    primary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            modifier = Modifier.size(30.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = primary,          // ✅ 체크 상태 녹색
                checkmarkColor = Color.White,
                uncheckedColor = Color(0xFF98A2AE) // 비활성 테두리 색(중간 회색)
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF111111))
    }
}

@Preview(showSystemUi = true, name = "Saving Terms (Green)")
@Composable
private fun SavingTermsPreview() {
    SavingTermsScreen()
}
