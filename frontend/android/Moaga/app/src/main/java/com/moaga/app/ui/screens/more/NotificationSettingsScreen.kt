package com.moaga.app.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(30.dp))
        Text(
            text = "알림 설정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingSwitchItem("플래너")
        SettingSwitchItem("리포트 발행")
        SettingSwitchItem("거래 내역 업데이트")
    }
}

@Composable
fun SettingSwitchItem(title: String) {
    var checked by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,          // 켜졌을 때 동그라미
                checkedTrackColor = Color(0xFF18A87E),   // 켜졌을 때 배경 (초록)
                uncheckedThumbColor = Color.White,       // 꺼졌을 때 동그라미
                uncheckedTrackColor = Color(0xFFCCCCCC), // 꺼졌을 때 배경 (회색)
                uncheckedBorderColor = Color.Gray,       // 꺼졌을 때 테두리
                checkedBorderColor = Color(0xFF18A87E)   // 켜졌을 때 테두리
            )
        )
    }
}

