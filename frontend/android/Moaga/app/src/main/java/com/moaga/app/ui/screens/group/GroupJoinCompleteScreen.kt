// file: app/src/main/java/com/d105/app/ui/screens/group/GroupJoinCompleteScreen.kt
package com.moaga.app.ui.screens.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R

@Composable
@Preview(showSystemUi = true)
fun GroupJoinCompleteScreen(
    onGoHome: () -> Unit = {}
) {
    val brandBlue = Color(0xFF18A87E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Image(
//                painter = painterResource(id = R.drawable.ssafy_logo),
//                contentDescription = "싸피 로고",
//                modifier = Modifier.size(28.dp)
//            )
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

        // 상단 여백(중앙으로 살짝 내리고 싶다면 유지)
        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = brandBlue,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(16.dp))

        // ✅ 줄간격 지정해서 겹침 방지
        Text(
            text = "가입 신청이\n완료되었어요!",
            fontSize = 36.sp,
            lineHeight = 44.sp,              // ← 줄간격
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111111),
            textAlign = TextAlign.Center
        )

        // ✅ 버튼을 글자 바로 아래에 배치
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
        ) {
            Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // 남는 공간은 아래로
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
    }
}