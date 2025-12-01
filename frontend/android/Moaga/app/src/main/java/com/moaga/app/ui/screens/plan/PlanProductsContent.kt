// file: app/src/main/java/com/moaga/app/ui/screens/plan/PlanProductsContent.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlanProductsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "플랜 상품 준비 중입니다",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}