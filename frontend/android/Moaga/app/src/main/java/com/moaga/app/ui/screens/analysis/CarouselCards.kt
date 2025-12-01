package com.moaga.app.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CarouselCards(cards: List<AnalysisCard>) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // 카드 간격 조정
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(cards) { index, card ->
            // 현재 보이는 아이템들의 정보
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo

            // 중앙 아이템을 기준으로 거리 계산
            val itemInfo = visibleItemsInfo.find { it.index == index }
            val distanceFromCenter = if (itemInfo != null) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val lazyRowCenter = listState.layoutInfo.viewportSize.width / 2
                abs(itemCenter - lazyRowCenter).toFloat()
            } else {
                Float.MAX_VALUE
            }

            // 거리에 따른 스케일 계산 (더 부드럽게 조정)
            val maxDistance = listState.layoutInfo.viewportSize.width / 2f
            val normalizedDistance = min(distanceFromCenter / maxDistance, 1f)

            // 스케일: 중앙 1.0, 가장자리 0.75 (더 작게)
            val scale = 1f - (normalizedDistance * 0.25f)

            // 알파: 중앙 1.0, 가장자리 0.5 (더 흐릿하게)
            val alpha = 1f - (normalizedDistance * 0.5f)

            // Y축 오프셋: 중앙은 0, 가장자리는 약간 아래로
            val yOffset = normalizedDistance * 8f

            AnalysisCardItem(
                card = card,
                scale = scale,
                alpha = alpha,
                yOffset = yOffset
            )
        }
    }
}