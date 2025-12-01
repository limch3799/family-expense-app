// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanOpenedDoneScreen.kt
package com.moaga.app.ui.screens.plan

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R

@Composable
fun PlanOpenedDoneScreen(
    onDone: () -> Unit,
    @DrawableRes centerImageRes: Int = R.drawable.account_opened_illustration,
    @DrawableRes logoRes: Int = R.drawable.ssafy_logo
) {
    // ✅ 파란색 계열 → 녹색 계열로統一
    val primaryGreen = Color(0xFF18A87E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // 상단 로고
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(logoRes), contentDescription = "로고", modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("싸피은행", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
        }

        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(centerImageRes),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("개설이 완료되었습니다!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
        Spacer(Modifier.height(20.dp))
        Text("모임통장 적금 개설이 성공적으로 완료되었어요.", fontSize = 16.sp, color = Color(0xFF111111))

        Spacer(Modifier.height(20.dp))

        // ✅ 완료 버튼만 1개
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("완료", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(8.dp))
    }
}
