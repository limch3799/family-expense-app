// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanApplyScreen.kt
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun PlanApplyScreen(
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {},
    productName: String = "단기 적금 플랜(1개월)",
    periodLabel: String = "2025-09-01 ~ 2025-09-30",
    rateLabel: String = "연 3.2%"
) {
    val primaryGreen = Color(0xFF18A87E)
    val inputBg      = Color(0xFFEAF6F2)
    val titleColor   = Color(0xFF111111)

    var pwd by remember { mutableStateOf("") }
    var pwdMsg by remember { mutableStateOf("숫자 4자리") }          // ✅ 변경
    var pwd2 by remember { mutableStateOf("") }
    var pwd2Msg by remember { mutableStateOf("일치여부 확인") }

    var goal by remember { mutableStateOf("1,000,000원") }
    var desc by remember { mutableStateOf("여름 휴가비 모으기") }
    var agree1 by remember { mutableStateOf(false) }
    var agree2 by remember { mutableStateOf(false) }
    var agree3 by remember { mutableStateOf(false) }

    // ✅ 정확히 4자리만 유효
    val pinValid   = pwd.length == 4
    val pinSame    = pwd2.length == 4 && pwd2 == pwd      // ✅ 4자리 모두 입력 + 일치
    val allChecked = agree1 && agree2 && agree3
    val enableBtn  = pinValid && pinSame && allChecked

    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("플랜 적금 개설 신청", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = titleColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = titleColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onComplete,
                    enabled = enableBtn,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp)
                .verticalScroll(scroll)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            FieldBlockFixed(label = "상품명") { ReadonlyField(productName, inputBg) }
            FieldBlockFixed(label = "상품기간") { ReadonlyField(periodLabel, inputBg) }
            FieldBlockFixed(label = "적용 금리") { ReadonlyField(rateLabel, inputBg) }

            // 계좌 비밀번호 (4자리 고정)
            FieldBlockFixed(label = "계좌 비밀번호", helperText = pwdMsg, helperColor = primaryGreen) {
                InputField(
                    value = pwd,
                    onValueChange = {
                        val only = it.filter(Char::isDigit).take(4) // ✅ 숫자만, 최대 4자리
                        pwd = only
                        pwdMsg = if (only.length == 4) "사용가능 확인" else "숫자 4자리"
                    },
                    placeholder = "****",
                    fieldBg = inputBg,
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword
                )
            }

            // 계좌 비밀번호 확인 (4자리 고정)
            FieldBlockFixed(label = "계좌 비밀번호 확인", helperText = pwd2Msg, helperColor = primaryGreen) {
                InputField(
                    value = pwd2,
                    onValueChange = {
                        val only = it.filter(Char::isDigit).take(4) // ✅ 숫자만, 최대 4자리
                        pwd2 = only
                        pwd2Msg = when {
                            only.isEmpty()      -> "일치여부 확인"
                            only.length < 4     -> "4자리 모두 입력"
                            only == pwd         -> "일치합니다"
                            else                -> "비밀번호가 일치하지 않습니다"
                        }
                    },
                    placeholder = "****",
                    fieldBg = inputBg,
                    isPassword = true,
                    isError = (pwd2.length == 4 && pwd2 != pwd), // ✅ 4자리 채웠는데 불일치면 에러
                    keyboardType = KeyboardType.NumberPassword
                )
            }

            FieldBlockFixed(label = "목표 금액") {
                InputField(
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = "예) 1,000,000원",
                    fieldBg = inputBg,
                    keyboardType = KeyboardType.Number
                )
            }

            FieldBlockFixed(label = "플랜 설명") {
                InputField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = "예) 여름 휴가비 모으기",
                    fieldBg = inputBg
                )
            }

            Spacer(Modifier.height(8.dp))

            Text("최종 약관 동의", fontSize = 13.sp, color = titleColor)
            AgreeRow("예금자보호법 안내 (필수)", agree1) { agree1 = it }
            AgreeRow("모임통장 운영규칙 동의 (필수)", agree2) { agree2 = it }
            AgreeRow("거래내역 공유 동의 (필수)", agree3) { agree3 = it }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ----- SignUpScreen 스타일 동일 유틸 ----- */

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadonlyField(value: String, fieldBg: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = fieldBg,
            focusedContainerColor = fieldBg,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fieldBg: Color,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text(placeholder, style = TextStyle(fontSize = 14.sp)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = fieldBg,
            focusedContainerColor = fieldBg,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun AgreeRow(text: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF18A87E),
                checkmarkColor = Color.White
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF111111))
    }
}
