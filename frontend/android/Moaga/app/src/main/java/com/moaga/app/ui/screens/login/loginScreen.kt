package com.moaga.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moaga.app.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onFindIdPwClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onSimplePinLoginClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isLoading: Boolean = false
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Lottie 애니메이션 설정
    val bearComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bear))

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 상단 중앙 로고와 로티 애니메이션
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    /*LottieAnimation(
                        composition = bearComposition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))*/
                    Image(
                        painter = painterResource(id = R.drawable.moaga_logo_color),
                        contentDescription = "모아가 로고",
                        modifier = Modifier
                            .size(210.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // 클릭 시 어두워지는 효과 제거
                            ) {
                                if (!isLoading) {
                                    onSimplePinLoginClick()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                // 아이디 입력 (이메일) - 파스텔 녹색
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    placeholder = { Text("이메일") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8F5E8), // 파스텔 녹색
                        focusedContainerColor = Color(0xFFE8F5E8),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // 비밀번호 입력 - 파스텔 녹색
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8F5E8), // 파스텔 녹색
                        focusedContainerColor = Color(0xFFE8F5E8),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // 로그인 버튼
                Button(
                    onClick = {
                        if (id.isBlank() || password.isBlank()) {
                            // 빈 필드가 있으면 아무것도 하지 않음 (또는 스낵바 표시)
                            scope.launch {
                                snackbarHostState.showSnackbar("아이디와 비밀번호를 모두 입력해주세요")
                            }
                            return@Button
                        }
                        onLoginClick(id, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // 아이디/비번 찾기
                Text(
                    text = "아이디나 비밀번호를 잊으셨나요?",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                    modifier = Modifier.clickable { onFindIdPwClick() }
                )



                // 회원가입 안내
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "계정이 없으신가요?", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "회원가입",
                        fontSize = 14.sp,
                        color = Color(0xFFFF6600),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSignUpClick() }
                    )
                }
            }


        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}