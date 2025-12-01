package com.moaga.app.ui.screens.analysis.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.moaga.app.R
import com.moaga.app.ui.theme.font_gothic_5
import kotlinx.coroutines.delay

@Composable
fun ReportLoadingScreen() {
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_block))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD4FAD6)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(210.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "리포트를 불러오는 중입니다${".".repeat(dotCount)}",
                fontSize = 21.sp,
                fontFamily = font_gothic_5,
                color = Color(0xFF111111)
            )
        }
    }
}