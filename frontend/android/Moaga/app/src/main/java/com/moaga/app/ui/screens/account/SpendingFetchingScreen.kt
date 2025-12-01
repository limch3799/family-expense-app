// file: app/src/main/java/com/moaga/app/ui/screens/group/create/SpendingFetchingScreen.kt
package com.moaga.app.ui.screens.group.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SpendingFetchingScreen(
    onDone: () -> Unit,
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo
) {
    // 3초 후 onDone 호출
    LaunchedEffect(Unit) {
        delay(3000)
        onDone()
    }

    val brandBlue = Color(0xFF18A87E)

    // Lottie 애니메이션 컴포지션
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_mydata))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        // 상단 로고
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Image(painter = painterResource(logoRes), contentDescription = "로고", modifier = Modifier.size(28.dp))
//            Spacer(Modifier.width(8.dp))
//            Text("싸피은행", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = brandBlue)
//        }

        Spacer(Modifier.weight(1f))

        // Lottie 애니메이션
        LottieAnimation(
            composition = composition.value,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(220.dp)
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "나의 지출 내역을 불러오는 중입니다...",
            fontSize = 20.sp,
            color = Color(0xFF111111),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun SpendingFetchingScreenPreview() {
    SpendingFetchingScreen(
        onDone = { /* Preview에서는 동작하지 않음 */ }
    )
}