package com.moaga.app.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalysisCardItem(
    card: AnalysisCard,
    scale: Float,
    alpha: Float,
    yOffset: Float
) {
    Card(
        modifier = Modifier
            .width(300.dp) // 카드 크기 약간 축소
            .height(300.dp)
            .scale(scale)
            .alpha(alpha)
            .offset(y = yOffset.dp), // 간단한 Y축 오프셋만 적용
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = card.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = card.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = card.textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜
            Text(
                text = card.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = card.textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 총 지출액 라벨
            Text(
                text = card.description,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 금액
            Text(
                text = card.value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
        }
    }
}