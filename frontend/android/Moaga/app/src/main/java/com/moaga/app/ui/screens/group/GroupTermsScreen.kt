// file: app/src/main/java/com/moaga/app/ui/screens/group/create/GroupTermsScreen.kt
package com.moaga.app.ui.screens.group.create

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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun GroupTermsScreen(
    onBack: () -> Unit = {},
    onAgree: () -> Unit = {}
) {
    val brandBlue = Color(0xFF18A87E)
    val termsBg   = Color(0xFFF2F2F2)   // ← 회색 배경

    var agree1 by remember { mutableStateOf(false) }
    var agree2 by remember { mutableStateOf(false) }
    var agree3 by remember { mutableStateOf(false) }
    val allChecked = agree1 && agree2 && agree3

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("그룹 계좌 생성", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("서비스 이용약관 동의", fontSize = 14.sp, color = Color(0xFF111111))

            // 약관 본문(스크롤) — 크기 확대 + 회색 배경
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // ← 크기 늘림
                    .background(termsBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                val scroll = rememberScrollState()
                Text(
                    text = """
        제1조(목적) 
        본 약관은 서비스를 이용함에 있어 회사와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.
        
        제2조(정의) 
        1. "서비스"라 함은 회사가 제공하는 모든 온라인 플랫폼을 말합니다.
        2. "회원"이라 함은 본 약관에 동의하고 서비스를 이용하는 자를 말합니다.
        
        제3조(서비스 이용) 
        회원은 회사가 정한 절차에 따라 서비스를 이용할 수 있습니다.
        
        제4조(개설 및 관리) 
        회원은 계정을 성실히 관리하여야 하며, 관리 소홀로 발생하는 문제에 대해 회사는 책임을 지지 않습니다.
        
    """.trimIndent(),
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.verticalScroll(scroll)
                )
            }

            Text("필수 동의 항목", fontSize = 14.sp, color = Color(0xFF111111))

            // ✅ 체크 항목들 사이 간격만 줄임(6dp)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                AgreeRow("모임통장 서비스 이용약관 동의 (필수)", agree1) { agree1 = it }
                AgreeRow("개인정보 수집 및 이용 동의 (필수)", agree2) { agree2 = it }
                AgreeRow("금융거래정보 제공 동의 (필수)", agree3) { agree3 = it }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAgree,
                enabled = allChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) {
                Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AgreeRow(text: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF111111))
    }
}