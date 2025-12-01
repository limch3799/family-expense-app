package com.moaga.app.ui.screens.group.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import kotlinx.coroutines.delay

@Composable
fun AccountOpeningScreen(
    onDone: () -> Unit,
    @DrawableRes centerImageRes: Int = R.drawable.account_opening_illustration, // 중앙 이미지(네가 넣은 drawable ID)
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo
) {
    val brandBlue = Color(0xFF18A87E)

    // 3초 후 다음 화면으로
    LaunchedEffect(Unit) {
        delay(3000)
        onDone()
    }

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
        Image(
            painter = painterResource(id = R.drawable.moaga_logo_color),
            contentDescription = "가족 이미지",
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .aspectRatio(1.0f),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(centerImageRes),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("계좌를 개설 중입니다..", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
    }
}
