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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.moaga.app.ui.theme.PrimaryColor

@Composable
fun AccountLinkPromptScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo,
    @DrawableRes leftIconRes: Int = R.drawable.ic_link_money,
    @DrawableRes rightIconRes: Int = R.drawable.ic_link_card
) {
    val brandBlue = Color(0xFF18A87E)
    val okGreen   = Color(0xFF14B866)
    val skipGray  = Color(0xFF8F9499)

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
        /*Image(
            painter = painterResource(id = R.drawable.moaga_logo_color),
            contentDescription = "가족 이미지",
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .aspectRatio(1.0f),
            contentScale = ContentScale.Fit
        )*/

        Spacer(Modifier.weight(1f))

        // 큰 초록 버튼
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ⬇️ 위: 이미지 2개 가로, 아래: 텍스트
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(leftIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Image(
                        painter = painterResource(rightIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "내 계좌/카드 불러오기",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // 건너뛰기(회색)
        Button(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = skipGray)
        ) {
            Text("건너뛰기", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showSystemUi = true, name = "Account Link Prompt")
@Composable
fun AccountLinkPromptPreview() {
    AccountLinkPromptScreen(
        onContinue = {},
        onSkip = {}
    )
}