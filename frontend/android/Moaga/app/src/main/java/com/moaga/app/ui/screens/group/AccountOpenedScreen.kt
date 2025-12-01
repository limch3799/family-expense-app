package com.moaga.app.ui.screens.group.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R

@Composable
fun AccountOpenedScreen(
    onYes: () -> Unit,
    onLater: () -> Unit,
    @DrawableRes centerImageRes: Int = R.drawable.account_opened_illustration, // 중앙 이미지(drawable)
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo
) {
    val brandBlue = Color(0xFF1062FF)
    val okGreen   = Color(0xFF14B866)

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
        Text("개설이 완료되었습니다!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
        Spacer(Modifier.height(20.dp))
        Text("본인의 계좌/카드를 등록하러 가볼까요?", fontSize = 16.sp, color = Color(0xFF111111))

        Spacer(Modifier.height(20.dp))

        // 네
        Button(
            onClick = onYes,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) { Text("네", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))

        // 나중에 (초록)
        Button(
            onClick = onLater,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = okGreen),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) { Text("나중에", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(8.dp))
    }
}
