// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanEmptyScreen.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_5

@Composable
fun PlanEmptyScreen(
    onCreatePlanClick: () -> Unit = {}
) {
    val primaryGreen = Color(0xFF18A87E)   // 메인 컬러
    val hintGray     = Color(0xFF9AA0AA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1.0f))

        Text(
            text = "생성된 플랜이 없습니다.",
            color = hintGray,
            fontSize = 18.sp,
            fontFamily = font_paperlogy_5
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCreatePlanClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "플랜 생성",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = font_gothic_5
            )
        }

        Spacer(Modifier.weight(1.4f))
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Plan Empty")
@Composable
private fun PlanEmptyScreenPreview() {
    PlanEmptyScreen()
}
