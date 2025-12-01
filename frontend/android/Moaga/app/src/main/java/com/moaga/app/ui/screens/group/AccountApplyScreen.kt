package com.moaga.app.ui.screens.group.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun AccountApplyScreen(
    onBack: () -> Unit = {},
    onDone: (name: String, pw: String) -> Unit = { _, _ -> }
) {
    val inputBg = Color(0xFFEAF6F2)
    val brandBlue = Color(0xFF18A87E)

    var groupName by remember { mutableStateOf("") } // 선택(옵션)
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var t1 by remember { mutableStateOf(false) }
    var t2 by remember { mutableStateOf(false) }
    var t3 by remember { mutableStateOf(false) }

    val pwValid = pw.length >= 4          // 필요시 규칙 변경
    val pwMatch = pw.isNotEmpty() && pw == pw2
    val termsOk = t1 && t2 && t3
    val enabled = pwValid && pwMatch && termsOk

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("계좌 개설 신청", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
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

            // 그룹계좌 이름(선택)
            Text("그룹계좌 이름(선택)", fontSize = 14.sp, color = Color(0xFF111111))
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it.take(15) },
                placeholder = { Text("그룹통장 이름을 입력해주세요. (최대 15자)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            // 비밀번호
            Text("계좌 비밀번호", fontSize = 14.sp, color = Color(0xFF111111))
            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it.take(6) },            // 최대 6자리 등 정책에 맞게 조정
                placeholder = { Text("****") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            // 비밀번호 확인
            Text("계좌 비밀번호 확인", fontSize = 14.sp, color = Color(0xFF111111))
            OutlinedTextField(
                value = pw2,
                onValueChange = { pw2 = it.take(6) },
                placeholder = { Text("****") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(48.dp))

            // 최종 약관 동의
            Text("최종 약관 동의", fontSize = 14.sp, color = Color(0xFF111111))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AgreeRow("예금자보호법 안내 (필수)", t1) { t1 = it }
                AgreeRow("모임통장 운영규칙 동의 (필수)", t2) { t2 = it }
                AgreeRow("거래내역 공유 동의 (필수)", t3) { t3 = it }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onDone(groupName.trim(), pw) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) { Text("완료", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold) }
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
