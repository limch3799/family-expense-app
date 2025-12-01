package com.moaga.app.ui.screens.expense

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moaga.app.R
import com.moaga.app.ui.theme.font_gothic_5

@Composable
fun BoxScope.ExpenseLoadingOverlay(
    showInitialLoading: Boolean
) {
    // 초기 로딩 오버레이
    AnimatedVisibility(
        visible = showInitialLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            val loadingComposition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading_dot)
            )
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}

@Composable
fun BoxScope.ExpenseFloatingComponents(
    showFab: Boolean,
    isFabExpanded: Boolean,
    onFabExpandToggle: () -> Unit,
    onDismissExpanded: () -> Unit
) {
    // 배경 오버레이 (FAB 확장 시)
    AnimatedVisibility(
        visible = isFabExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismissExpanded() }
        )
    }

    // 오른쪽 아래 FAB - 스크롤 상태에 따라 애니메이션
    AnimatedVisibility(
        visible = showFab,
        enter = slideInVertically(
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ) + fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                end = 24.dp,
                bottom = 120.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = "마이데이터 연결",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = font_gothic_5
                )
            }

            FloatingActionButton(
                onClick = onFabExpandToggle,
                containerColor = if (isFabExpanded) Color.White else Color(0xFF18A87E),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFabExpanded) Icons.Default.Description else Icons.Default.Add,
                    contentDescription = "FAB",
                    tint = if (isFabExpanded) Color.Black else Color.White
                )
            }
        }
    }
}