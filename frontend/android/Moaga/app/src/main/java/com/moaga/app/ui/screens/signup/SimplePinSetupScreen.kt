package com.moaga.app.ui.screens.pin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private const val PIN_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun SimplePinSetupScreen(
    onBack: () -> Unit = {},
    onComplete: (String) -> Unit = {}
) {
    val primaryBlue = Color(0xFF1062FF)
    val inputBg = Color(0xFFEFF2FF)

    var pin by remember { mutableStateOf("") }
    val shuffled = remember { (0..9).map { it.toString() }.shuffled() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth().padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("간편 비밀번호 설정", fontSize = 18.sp, color = Color.Black) }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
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
                .padding(horizontal = 24.dp)
        ) {
            // ✅ 남은 공간의 정중앙에 입력 영역 배치
            Box(
                modifier = Modifier
                    .weight(1f)               // 상단 영역이 남은 공간 차지
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("비밀번호를 입력해주세요", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("숫자 6자리", fontSize = 14.sp, color = Color.Gray)

                    Spacer(Modifier.height(24.dp))
                    PinDots(length = PIN_LENGTH, filled = pin.length, dotSize = 14.dp, space = 16.dp, color = Color.DarkGray)

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "비밀번호를 잊으셨나요?",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
            }

            // ✅ 가로 전체를 꽉 채우는 랜덤 키패드
            RandomKeypad(
                digits = shuffled,
                onDigit = { d ->
                    if (pin.length < PIN_LENGTH) {
                        pin += d
                        if (pin.length == PIN_LENGTH) onComplete(pin)
                    }
                },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                onClearAll = { pin = "" },
                keyTextSize = 28.sp,
                rowHeight = 64.dp,           // 줄 높이 (원하면 72~82dp로 더 키워도 OK)
                horizontalSpacing = 16.dp,
                inputBg = inputBg
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** ●●●●●● 표시 */
@Composable
fun PinDots(length: Int, filled: Int, dotSize: Dp, space: Dp, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(space)) {
        repeat(length) { i ->
            val filledNow = i < filled
            Canvas(
                modifier = Modifier
                    .size(dotSize)
                    .background(Color.Transparent, CircleShape)
            ) {
                if (filledNow) drawCircle(color = color)
                else drawCircle(color = color.copy(alpha = 0.5f), style = Stroke(width = 2f))
            }
        }
    }
}

/** ✅ 가로 꽉 차는 3x4 랜덤 숫자 키패드 */
@Composable
fun RandomKeypad(
    digits: List<String>,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    keyTextSize: androidx.compose.ui.unit.TextUnit,
    rowHeight: Dp,
    horizontalSpacing: Dp,
    inputBg: Color
) {
    val r1 = digits.subList(0, 3)
    val r2 = digits.subList(3, 6)
    val r3 = digits.subList(6, 9)
    val last = digits[9]

    @Composable
    fun RowOfKeys(items: List<String>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { d ->
                NumKey(
                    label = d,
                    onClick = { onDigit(d) },
                    modifier = Modifier
                        .weight(1f)            // ⬅️ 각 키가 너비 1/3씩 차지
                        .height(rowHeight),
                    textSize = keyTextSize
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

        // 마지막 줄: 전체삭제 | 0(랜덤) | 백스페이스 — 전부 가로 균등 분배
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClearAll,
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
            ) {
                Text("전체삭제", fontSize = 14.sp, textAlign = TextAlign.Center)
            }

            NumKey(
                label = last,
                onClick = { onDigit(last) },
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                textSize = keyTextSize
            )

            IconButton(
                onClick = onBackspace,
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,          // ✅ 배경 없음
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Transparent   // 비활성도 투명
                )
            ) {
                Icon(Icons.Outlined.Backspace, contentDescription = "지우기")
            }
        }
    }
}

@Composable
private fun NumKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,                       // ⬅️ weight/height 그대로 사용
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, fontSize = textSize, color = Color.Black, textAlign = TextAlign.Center)
    }
}
