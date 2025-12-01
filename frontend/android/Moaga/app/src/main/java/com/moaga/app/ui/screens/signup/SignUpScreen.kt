// file: app/src/main/java/com/d105/app/ui/screens/signup/SignUpScreen.kt
package com.moaga.app.ui.screens.signup

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun SignUpScreen(
    onBack: () -> Unit = {},
    onSendEmailCode: (String) -> Unit = {},
    onVerifyEmailCode: (String, String) -> Unit = { _, _ -> },
    onNext: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    emailCodeMsg: String = ""
) {
    var email by remember { mutableStateOf("") }
    var emailMsg by remember { mutableStateOf("사용 가능한 이메일입니다.") }

    var password by remember { mutableStateOf("") }
    var passwordMsg by remember { mutableStateOf("사용가능 확인") }

    var passwordConfirm by remember { mutableStateOf("") }
    var passwordConfirmMsg by remember { mutableStateOf("일치여부 확인") }

    var name by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }

    var emailCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }

    val inputBg = Color(0xFFEAF6F2)
    val primaryBlue = Color(0xFF18A87E)
    val buttonBlue = Color(0xFF1062FF)
    var isEmailVerified by remember { mutableStateOf(false) }
    LaunchedEffect(emailCodeMsg) {
        if (emailCodeMsg == "인증 완료") {
            isEmailVerified = true
        }
    }

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
                        Text("회원가입", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { onNext(email, password, name, birth) },
                    enabled =
//                            isEmailVerified &&   // ✅ 이메일 인증 성공했는지 확인
                            email.isNotBlank() &&
                            password.length >= 8 &&
                            password == passwordConfirm &&
                            name.isNotBlank() &&
                            birth.length == 8,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp) // 각 블록 간격 일정
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // 이메일
            FieldBlockFixed(
                label = "이메일",
                helperText = emailMsg,
                helperColor = primaryBlue
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = { Text("이메일") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // 이메일 인증
            FieldBlockFixed(
                label = "이메일 인증",
                helperText = emailCodeMsg,
                helperColor = primaryBlue
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = emailCode,
                        onValueChange = { emailCode = it },
                        placeholder = { Text("인증코드 입력") },
                        singleLine = true,
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

                    val btnText = if (codeSent) "확인" else "발송"
                    Button(
                        onClick = {
                            if (!codeSent) {
                                onSendEmailCode(email)
                                codeSent = true
                            } else {
                                onVerifyEmailCode(email, emailCode)
                            }
                        },
                        enabled = if (!codeSent) email.isNotBlank() else emailCode.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text(btnText, color = Color.White)
                    }
                }
            }

            // 비밀번호
            FieldBlockFixed(
                label = "비밀번호",
                helperText = passwordMsg,
                helperColor = primaryBlue
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordMsg =
                            if (isValidPassword(it)) "사용 가능 확인"
                            else "영문+숫자+특수문자 8자리 이상"
                    },
                    placeholder = { Text("영문+숫자+특수문자 8자리 이상") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // 비밀번호 확인
            FieldBlockFixed(
                label = "비밀번호 확인",
                helperText = passwordConfirmMsg
            ) {
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = {
                        passwordConfirm = it
                        passwordConfirmMsg =
                            if (password == it && it.isNotEmpty()) "일치합니다" else "비밀번호를 다시 입력해주세요"
                    },
                    placeholder = { Text("비밀번호를 다시 입력해주세요") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // 이름
            FieldBlockFixed(label = "이름") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // 생년월일
            FieldBlockFixed(label = "생년월일") {
                OutlinedTextField(
                    value = birth,
                    onValueChange = { input ->
                        // ✅ 숫자만 허용 + 8자리 제한
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            birth = input
                        }
                    },
                    placeholder = { Text("예: 19980721") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = inputBg,
                        focusedContainerColor = inputBg,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

/* ----- 재사용 가능한 블록 (라벨 + 필드 + helper 고정 영역) ----- */
@Composable
private fun FieldBlockFixed(
    label: String,
    helperText: String? = null,
    helperColor: Color = Color.Gray,
    helperMinHeight: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF111111))
        content()
        // helper 문구가 없어도 동일 높이 확보
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = helperMinHeight)
                .padding(top = 1.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (!helperText.isNullOrEmpty()) {
                Text(
                    text = helperText,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = helperColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun isValidPassword(password: String): Boolean {
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    return password.length >= 8 && hasLetter && hasDigit && hasSpecial
}