package com.moaga.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.moaga.app.R
import com.airbnb.lottie.compose.*
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_paperlogy_6
import com.moaga.app.ui.theme.font_paperlogy_8
import com.moaga.app.ui.theme.moaga_primary_bold
import com.moaga.app.ui.theme.moaga_primary_medium
import com.moaga.app.ui.theme.moaga_two_bold

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bear))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(composition) {
        if (composition != null) {
            delay(3000)
            onTimeout()
        }
    }

    // 로티가 로딩되지 않았으면 아무것도 표시 안함 (또는 로딩 화면)
    if (composition == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
            //.background(Color(0xFFC0F5E5))
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        //.background(Color(0xFFC0F5E5)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.simplepinbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: Lottie 애니메이션 + 텍스트들
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = 30.dp) // 오른쪽으로 30dp 이동
                )


                // 로티 이미지 바로 밑 두 번째 텍스트
                Text(
                    text = "가족 지출 공유 및 분석 서비스",
                    style = TextStyle(
                        fontFamily = font_paperlogy_6,
                        fontSize = 18.sp,
                        color = Color(0xFF494949)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))


                // 로티 이미지 바로 밑 첫 번째 텍스트
                Text(
                    text = "모아가",
                    style = TextStyle(
                        fontFamily = moaga_primary_medium,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF494949)
                    )
                )
            }

            // 하단: 모아가 로고
            /*Image(
                painter = painterResource(id = R.drawable.moaga_logo_color),
                contentDescription = "모아가 로고",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )*/
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}