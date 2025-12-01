/*
// file: app/src/main/java/com/moaga/app/ui/screens/plan/PastPlansScreen.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPlansScreen(
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {}
) {
    val titleColor = Color(0xFF111111)
    val subtitle  = Color(0xFF6C7682)
    val cardBg    = Color(0xFFF8FAF9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("지난 플랜", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = titleColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            items(samplePastPlans()) { p ->
                Surface(
                    color = cardBg,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onOpenDetail(p.id) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(p.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                            Spacer(Modifier.height(4.dp))
                            Text(p.period, fontSize = 13.sp, color = subtitle)
                        }
                        Text(p.savedAmount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18A87E))
                    }
                }
            }
        }
    }
}
*/
