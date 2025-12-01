// file: app/src/main/java/com/d105/app/ui/screens/signup/SignUpCompleteScreen.kt
package com.moaga.app.ui.screens.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R

@Composable
@Preview(showSystemUi = true)
fun SignUpCompleteScreen(
    name: String = "동창현",
    onGoToLogin: () -> Unit = {},
    @DrawableRes celebrateRes: Int = R.drawable.celebrate
) {
    val brandGreen = Color(0xFF18A87E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // 앱 로고
            Image(
                painter = painterResource(id = R.drawable.moaga_logo_color),
                contentDescription = "로고",
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .aspectRatio(1.0f),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(28.dp))

            // 축하 이미지
            Image(
                painter = painterResource(id = celebrateRes),
                contentDescription = "축하 이미지",
                modifier = Modifier.size(180.dp)
            )

            Spacer(Modifier.height(24.dp))

            val welcome = buildAnnotatedString {
                withStyle(SpanStyle(color = brandGreen, fontWeight = FontWeight.Bold)) { append(name) }
                append("님, 환영합니다!")
            }
            Text(welcome, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(48.dp))
            Text("가입이 완료되었습니다.", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))

            Spacer(Modifier.height(28.dp))

            // ✅ 로그인 화면으로 가기 버튼
            PrimaryBigButton(
                text = "로그인 화면으로 가기",
                onClick = onGoToLogin,
                container = brandGreen
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PrimaryBigButton(
    text: String,
    onClick: () -> Unit,
    container: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}