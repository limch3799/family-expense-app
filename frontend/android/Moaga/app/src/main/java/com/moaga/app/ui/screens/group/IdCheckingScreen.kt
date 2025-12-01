// file: app/src/main/java/com/moaga/app/ui/screens/group/create/IdCheckingScreen.kt
package com.moaga.app.ui.screens.group.create

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moaga.app.data.api.IdResponse
import kotlinx.coroutines.delay

@Composable
fun IdCheckingScreen(
    uri: Uri,
    response: IdResponse?,
    onDone: (IdResponse?) -> Unit
) {
    LaunchedEffect(Unit) {
        // 1~2초 정도 로딩 애니메이션 보여주기
        kotlinx.coroutines.delay(1500)
        // OCR 결과 전달
        onDone(response)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("신분증 정보를 확인 중입니다...")
    }
}
