// file: app/src/main/java/com/moaga/app/ui/screens/plan/PastPlanContent.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PastPlanSummary(
    val id: String,
    val name: String,
    val period: String,      // 예: 2025.05.01 ~ 2025.06.30
    val savedAmount: String, // 예: 1,000,000원
)

fun samplePastPlans(): List<PastPlanSummary> = listOf(
    PastPlanSummary("p1", "봄 캠핑 준비", "2025.03.01 ~ 2025.04.30", "800,000원"),
    PastPlanSummary("p2", "노트북 업그레이드", "2025.01.01 ~ 2025.02.28", "1,500,000원"),
    PastPlanSummary("p3", "추석 용돈", "2024.08.15 ~ 2024.09.20", "600,000원"),
)

@Composable
fun PastPlanContent(onOpenPastPlanDetail: (String) -> Unit) {
    val titleColor = Color(0xFF111111)
    val subtitle = Color(0xFF6C7682)
    val cardBg = Color(0xFFF8FAF9)
    val pastPlans = samplePastPlans()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        items(pastPlans) { plan ->
            Surface(
                color = cardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onOpenPastPlanDetail(plan.id) }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            plan.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            plan.period,
                            fontSize = 13.sp,
                            color = subtitle
                        )
                    }
                    Text(
                        plan.savedAmount,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF18A87E)
                    )
                }
            }
        }
    }
}