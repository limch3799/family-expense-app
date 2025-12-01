// file: app/src/main/java/com/d105/app/ui/screens/verify/PhoneVerificationScreen.kt
package com.moaga.app.ui.screens.verify

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun PhoneVerificationScreen(
    onBack: () -> Unit = {},
    onSendSmsCode: (String) -> Unit = {},
    onVerifySmsCode: (String, String) -> Unit = { _, _ -> },
    onNext: (String) -> Unit = {},
    smsMsg: String = ""
) {
    val inputBg = Color(0xFFEAF6F2)
    val primaryBlue = Color(0xFF18A87E)

    // 숫자만 들고 있는 실제 값
    var phoneDigits by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }

    // 타이머
    var secondsLeft by remember { mutableStateOf(0) }
    LaunchedEffect(secondsLeft > 0) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    val phoneValid = phoneDigits.length in 10..11
    val codeValid = code.isNotBlank() // 필요하면 code.length == 6 등으로 변경

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("전화번호 인증", fontSize = 20.sp, color = Color.Black)
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
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("전화번호", fontSize = 14.sp, color = Color(0xFF111111))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = phoneDigits, // ✅ 항상 숫자 원본
                    onValueChange = { new ->
                        // 숫자만 허용 + 11자리 제한
                        phoneDigits = new.filter(Char::isDigit).take(11)
                    },
                    placeholder = { Text("010 - 0000 - 0000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    // ✅ 화면 표시만 하이픈 포맷, 커서 점프 없음
                    visualTransformation = PhoneNumberVisualTransformation()
                )
                Button(
                    onClick = {
                        onSendSmsCode(phoneDigits)
                        secondsLeft = 120
                    },
                    enabled = phoneValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) { Text("전송", color = Color.White) }
            }

            Text("인증번호 확인", fontSize = 14.sp, color = Color(0xFF111111))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        // 숫자만 허용 (원하면 길이 제한 추가)
                        code = input.filter(Char::isDigit)
                    },
                    placeholder = { Text("인증코드 입력") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                Button(
                    onClick = {
                        onVerifySmsCode(phoneDigits, code)
                        isVerified = true // 실제 검증 결과를 반영하려면 콜백에서 상태를 내려받아 업데이트하세요.
                        secondsLeft = 0
                    },
                    enabled = codeValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) { Text("확인", color = Color.White) }
            }

            if (secondsLeft > 0) {
                Text("남은시간  ${formatSeconds(secondsLeft)}", color = primaryBlue, fontSize = 12.sp)
            } else {
                Spacer(Modifier.height(18.dp))
            }

            Button(
                onClick = { onNext(phoneDigits) },
                enabled = isVerified,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .navigationBarsPadding()
                    .padding(top = 16.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("다음", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

/**
 * 화면에만 "010 - 0000 - 0000" 형태로 보이도록 하는 변환.
 * 내부 값은 숫자만 유지된다.
 */
private class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text // 숫자만 들어있다고 가정
        val out = StringBuilder()

        // 3-4-4 그룹으로 포맷하며 " - " 삽입
        for (i in raw.indices) {
            out.append(raw[i])
            if (i == 2 && raw.length > 3) out.append(" - ")
            if (i == 6 && raw.length > 7) out.append(" - ")
        }

        val firstSepAt = 3      // 원본 오프셋
        val secondSepAt = 7     // 원본 오프셋
        val sepLen = 3          // " - " 길이

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var t = offset
                if (raw.length > 3 && offset > firstSepAt) t += sepLen
                if (raw.length > 7 && offset > secondSepAt) t += sepLen
                return t
            }

            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                // 첫 번째 구분자 영역 보정
                if (raw.length > 3 && offset > firstSepAt) {
                    o -= minOf(sepLen, offset - firstSepAt)
                }
                // 두 번째 구분자 영역 보정
                if (raw.length > 7 && offset > secondSepAt + sepLen) {
                    o -= minOf(sepLen, offset - (secondSepAt + sepLen))
                }
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

private fun formatSeconds(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return String.format("%02d:%02d", m, sec)
}
