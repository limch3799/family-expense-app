// file: app/src/main/java/com/moaga/app/ui/screens/plan/CustomTabLayout.kt
package com.moaga.app.ui.screens.plan

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_gothic_5

@Composable
fun CustomTabLayout(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    primaryColor: Color,
    titleColor: Color
) {
    val density = LocalDensity.current

    Column {
        val tabWidths = remember { mutableStateListOf<Float>() }
        val tabPositions = remember { mutableStateListOf<Float>() }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                Text(
                    text = tab,
                    fontSize = 19.sp,
                    fontFamily = font_gothic_5,
                    color = if (selectedTabIndex == index) Color(0xFF393939) else Color(0xFFC6C7C9),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            val textWidth = coordinates.size.width.toFloat()

                            // 리스트 크기 조정
                            while (tabWidths.size <= index) {
                                tabWidths.add(0f)
                                tabPositions.add(0f)
                            }

                            tabWidths[index] = textWidth

                            // 위치 계산: 이전 탭들의 전체 너비(텍스트 + 패딩) 합계 + 현재 탭의 왼쪽 패딩
                            var position = 16f * density.density // 첫 번째 탭의 왼쪽 패딩
                            for (i in 0 until index) {
                                if (i < tabWidths.size) {
                                    position += tabWidths[i] + 32f * density.density // 텍스트 너비 + 좌우 패딩
                                }
                            }
                            tabPositions[index] = position
                        }
                )
            }
        }

        // 애니메이션이 적용된 인디케이터
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFFFFFFF))
        ) {
            if (tabWidths.size > selectedTabIndex && tabPositions.size > selectedTabIndex) {
                val indicatorOffset by animateDpAsState(
                    targetValue = with(density) { tabPositions[selectedTabIndex].toDp() },
                    label = "indicator_offset"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = with(density) { tabWidths[selectedTabIndex].toDp() },
                    label = "indicator_width"
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(indicatorWidth)
                        .height(4.dp)
                        .background(primaryColor)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomTabLayoutPreview() {
    CustomTabLayout(
        tabs = listOf("탭 1", "탭 2", "탭 3"),
        selectedTabIndex = 0,
        onTabSelected = { },
        primaryColor = Color(0xFF18A87E), // 원하는 색상으로 변경
        titleColor = Color(0xFF393939)
    )
}