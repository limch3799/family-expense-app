package com.moaga.app.ui.screens.pin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import com.moaga.app.R
import com.moaga.app.ui.theme.font_gothic_2
import com.moaga.app.ui.theme.font_gothic_4
import com.moaga.app.ui.theme.font_gothic_5
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplePinLoginScreen(
    email: String = "",
    isLoading: Boolean = false,
    onBackToPasswordLogin: () -> Unit = {},
    onPinCompleted: (String) -> Unit = {},
    // 두 번째 방식 호환용 파라미터들
    onComplete: (String) -> Unit = onPinCompleted,
    onGoToLoginScreen: () -> Unit = onBackToPasswordLogin,
    // 실제 로그인 로직을 위한 콜백
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    // 스낵바 표시를 위한 콜백
    onShowSnackbar: (String) -> Unit = {},
    // 개발/테스트용 플래그
    showTestButtons: Boolean = true
) {
    val primaryGreen = Color(0xFF18A87E)
    val lightGreen = Color(0xFF32DEAC)

    var pin by remember { mutableStateOf("") }
    val shuffled = remember { (0..9).map { it.toString() }.shuffled() }

    // 화면 로딩 애니메이션을 위한 상태
    val slideUpOffset = remember { Animatable(700f) }
    val fadeAlpha = remember { Animatable(0f) }

    // 화면이 처음 로드될 때 애니메이션 실행
    LaunchedEffect(Unit) {
        launch {
            slideUpOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        launch {
            fadeAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
    }

    // 테스트 로그인 함수
    fun performTestLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            onShowSnackbar("아이디와 비밀번호를 모두 입력해주세요")
            return
        }
        onLoginClick(email, password)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 백그라운드 이미지
        Image(
            painter = painterResource(id = R.drawable.simplepinbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )



        // 메인 스크린
        Scaffold(
            containerColor = Color.Transparent
        ) { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 24.dp)
                    .padding(top = 50.dp)
                    .graphicsLayer(
                        translationY = slideUpOffset.value,
                        alpha = fadeAlpha.value
                    )
            ) {
                // 입력 영역 배치
                Spacer(Modifier.height(66.dp))

                Text("간편 비밀번호 인증",
                    fontSize = 24.sp,
                    fontFamily = font_gothic_5,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isLoading) {
                                pin = "123456"
                                onPinCompleted("123456")
                                onComplete("123456")
                            }
                        },
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))

                Text("비밀번호를 입력해 주세요.",
                    fontSize = 14.sp,
                    fontFamily = font_gothic_4,
                    color = Color(0xFF41F8C3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // 아빠 계정으로 로그인
                            onLoginClick("realFather@naver.com", "dkQk123!")
                        },
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // 딸 계정으로 로그인
                            onLoginClick("realDaughter@naver.com", "shhj0228!")
                        },
                    horizontalArrangement = Arrangement.Center
                ) {
                    PinDots(
                        length = PIN_LENGTH,
                        filled = pin.length,
                        dotSize = 14.dp,
                        space = 16.dp,
                        emptyColor = Color(0xFF41F8C3),
                        filledColor = Color.White
                    )
                }

                Spacer(Modifier.height(32.dp))

                if (isLoading) {
                    Spacer(Modifier.height(16.dp))

                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_block))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(128.dp)
                        )
                    }
                }

                // 남은 공간을 모두 차지하는 스페이서 (키패드를 화면 하단으로 밀어냄)
                Spacer(Modifier.weight(1f))

                // 랜덤 키패드
                RandomKeypad(
                    digits = shuffled,
                    onDigit = { d ->
                        if (pin.length < PIN_LENGTH && !isLoading) {
                            pin += d
                            if (pin.length == PIN_LENGTH) {
                                onPinCompleted(pin)
                                onComplete(pin)
                            }
                        }
                    },
                    onBackspace = { if (pin.isNotEmpty() && !isLoading) pin = pin.dropLast(1) },
                    onClearAll = { if (!isLoading) pin = "" },
                    keyTextSize = 28.sp,
                    rowHeight = 64.dp,
                    horizontalSpacing = 16.dp,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(24.dp))

                // 버튼 이미지 추가
                Image(
                    painter = painterResource(id = R.drawable.button_simplepin),
                    contentDescription = "로그인페이지돌아가기",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBackToPasswordLogin()
                            onGoToLoginScreen()
                        }
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PinDots(
    length: Int,
    filled: Int,
    dotSize: Dp,
    space: Dp,
    emptyColor: Color,
    filledColor: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space)) {
        repeat(length) { i ->
            val filledNow = i < filled
            Canvas(
                modifier = Modifier
                    .size(dotSize)
                    .background(Color.Transparent, CircleShape)
            ) {
                if (filledNow) {
                    drawCircle(color = filledColor)
                } else {
                    drawCircle(color = emptyColor, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }
    }
}

/** 가로 꽉 차는 3x4 랜덤 숫자 키패드 */
@Composable
private fun RandomKeypad(
    digits: List<String>,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    keyTextSize: androidx.compose.ui.unit.TextUnit,
    rowHeight: Dp,
    horizontalSpacing: Dp,
    enabled: Boolean = true
) {
    val r1 = digits.subList(0, 3)
    val r2 = digits.subList(3, 6)
    val r3 = digits.subList(6, 9)
    val last = digits[9]

    // 각 숫자별 애니메이션 상태 관리
    val scaleStates = remember {
        digits.associateWith { Animatable(1f) }
    }
    val coroutineScope = rememberCoroutineScope()

    @Composable
    fun RowOfKeys(items: List<String>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { d ->
                NumKeyWithRandomAnimation(
                    label = d,
                    onClick = {
                        if (enabled) {
                            onDigit(d)
                            // 눌린 숫자와 랜덤으로 선택된 2개 숫자 애니메이션
                            val otherDigits = digits.filter { it != d }.shuffled().take(2)
                            val animateDigits = listOf(d) + otherDigits

                            coroutineScope.launch {
                                animateDigits.forEach { digit ->
                                    launch {
                                        scaleStates[digit]?.let { scale ->
                                            scale.animateTo(
                                                targetValue = 1.5f,
                                                animationSpec = tween(durationMillis = 100)
                                            )
                                            scale.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 100)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    textSize = keyTextSize,
                    enabled = enabled,
                    scale = scaleStates[d]?.value ?: 1f
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RowOfKeys(r1)
        RowOfKeys(r2)
        RowOfKeys(r3)

        // 마지막 줄: 전체삭제 | 0(랜덤) | 백스페이스
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedTextButton(
                text = "전체삭제",
                onClick = { if (enabled) onClearAll() },
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                enabled = enabled
            )

            NumKeyWithRandomAnimation(
                label = last,
                onClick = {
                    if (enabled) {
                        onDigit(last)
                        // 눌린 숫자와 랜덤으로 선택된 2개 숫자 애니메이션
                        val otherDigits = digits.filter { it != last }.shuffled().take(2)
                        val animateDigits = listOf(last) + otherDigits

                        coroutineScope.launch {
                            animateDigits.forEach { digit ->
                                launch {
                                    scaleStates[digit]?.let { scale ->
                                        scale.animateTo(
                                            targetValue = 1.5f,
                                            animationSpec = tween(durationMillis = 100)
                                        )
                                        scale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(durationMillis = 100)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                textSize = keyTextSize,
                enabled = enabled,
                scale = scaleStates[last]?.value ?: 1f
            )

            AnimatedIconButton(
                onClick = { if (enabled) onBackspace() },
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Backspace, contentDescription = "지우기", tint = Color(0xFF18A87E))
            }
        }
    }
}

@Composable
private fun NumKeyWithRandomAnimation(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    textSize: androidx.compose.ui.unit.TextUnit,
    enabled: Boolean = true,
    scale: Float
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // ripple 효과 제거
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = textSize,
            fontFamily = font_gothic_2,
            textAlign = TextAlign.Center,
            color = if (enabled) Color(0xFF16926E) else Color(0xFF16926E).copy(alpha = 0.5f),
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun AnimatedTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // ripple 효과 제거
            ) {
                if (enabled) {
                    onClick()
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 1.1f,
                            animationSpec = tween(durationMillis = 100)
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 100)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = if (enabled) Color(0xFF18A87E) else Color(0xFF18A87E).copy(alpha = 0.5f),
            modifier = Modifier.scale(scale.value)
        )
    }
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // ripple 효과 제거
            ) {
                if (enabled) {
                    onClick()
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 1.5f,
                            animationSpec = tween(durationMillis = 100)
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 100)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.scale(scale.value),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) Color(0xFF18A87E) else Color(0xFF18A87E).copy(alpha = 0.5f)
            ) {
                content()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SimplePinLoginScreenPreview() {
    SimplePinLoginScreen(
        email = "test@example.com",
        isLoading = false,
        onBackToPasswordLogin = {},
        onPinCompleted = {},
        onLoginClick = { email, password ->
            println("Test Login: $email / $password")
        },
        onShowSnackbar = { message ->
            println("Snackbar: $message")
        },
        showTestButtons = true
    )
}