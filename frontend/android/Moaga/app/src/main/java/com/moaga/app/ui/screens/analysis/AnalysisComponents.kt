package com.moaga.app.ui.screens.analysis

import com.moaga.app.R
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.moaga.app.ui.theme.font_gothic_4
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6
import com.moaga.app.ui.theme.font_paperlogy_7
import com.moaga.app.ui.theme.moaga_primary_bold
import com.moaga.app.ui.theme.moaga_primary_medium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 분석 카드 데이터 클래스
data class AnalysisCard(
    val title: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val textColor: Color
)

@Composable
fun FamilyReportCard(
    groupInfo: FamilyGroupInfo?,
    expenseInfo: GroupExpenseInfo?
) {
    var selectedProfileIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null, // 클릭 애니메이션 제거
                interactionSource = remember { MutableInteractionSource() }
            ) { selectedProfileIndex = null }, // 카드 클릭시 툴팁 닫기
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "가족그룹",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = groupInfo?.groupName ?: "로딩중...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "멤버",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Box(
                    modifier = Modifier.padding(start = 40.dp) // 오른쪽으로 더 밀어주기
                ) {
                    Row {
                        groupInfo?.members?.forEachIndexed { index, member ->
                            Image(
                                painter = painterResource(id = member.profileImageRes),
                                contentDescription = "프로필",
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(x = (-4 * index).dp)
                                    .background(Color.Gray, CircleShape)
                                    .padding(0.7.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedProfileIndex = if (selectedProfileIndex == index) null else index
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // 말풍선 툴팁
                    selectedProfileIndex?.let { index ->
                        groupInfo?.members?.getOrNull(index)?.let { member ->
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = (24 * index - 4 * index).dp - 12.dp, // 프로필 이미지 중앙에 맞추기
                                        y = (-30).dp // 프로필 위에 표시 (조금 더 아래로)
                                    )
                                    .wrapContentHeight(unbounded = true) // 레이아웃에 영향 주지 않도록
                            ) {
                                // 말풍선 배경
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Text(
                                        text = member.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }

                                // 말풍선 꼬리 (삼각형)
                                Canvas(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .offset(x = 12.dp, y = 16.dp) // 말풍선과 연결되도록 위치 조정
                                ) {
                                    val path = Path().apply {
                                        moveTo(size.width / 2, size.height)
                                        lineTo(0f, 0f)
                                        lineTo(size.width, 0f)
                                        close()
                                    }
                                    drawPath(path, color = androidx.compose.ui.graphics.Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "이번 달 지출",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = expenseInfo?.let {
                        buildAnnotatedString {
                            append("-")
                            withStyle(style = SpanStyle(fontSize = 16.sp)) { // 금액 부분만 크게
                                append("${String.format("%,d", it.totalAmount)}원")
                            }
                            append(" (${it.startDate}~${it.endDate})")
                        }
                    } ?: AnnotatedString("로딩중..."),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReportActionCard(
    title: String,
    iconRes: Int,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "리포트 아이콘",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontFamily = font_paperlogy_6,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentReportsSection(
    cards: List<AnalysisCard>,
    recentReports: List<ReportItem> = emptyList(),
    onReportClick: (Int, Int, String) -> Unit = { _, _, _ -> } // reportId, reportType, date 순서로 변경
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 최근 생성 리포트 제목
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "   최근 생성 리포트",
                fontSize = 21.sp,
                fontFamily = font_gothic_4,
                color = Color(0xFF343434)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 리포트 카드들 (수평 스크롤)
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.width(4.dp)) // 시작 패딩

            cards.forEachIndexed { index, card ->
                val alpha = when {
                    index < 2 -> 1f // 첫 2개는 항상 선명
                    else -> {
                        // 3번째부터는 스크롤 상태에 따라 투명도 결정
                        val scrollProgress = scrollState.value.toFloat()
                        val fadeStartPosition = (index - 2) * 140f // 카드 위치 계산
                        when {
                            scrollProgress >= fadeStartPosition -> 1f
                            scrollProgress >= fadeStartPosition - 100f -> {
                                (scrollProgress - (fadeStartPosition - 100f)) / 100f
                            }
                            else -> 0.3f // 기본적으로 흐릿하게
                        }
                    }
                }

                val reportItem = recentReports.getOrNull(index)

                ReportCard3DFold(
                    yearMonth = card.title,
                    borderColor = card.color,
                    showRedDot = index == 0,  // 첫 번째 카드만 빨간 점 표시
                    onClick = {
                        reportItem?.let { report ->
                            // reportId, reportType(2로 하드코딩), date(YYYY-MM 형식으로 변환)
                            val dateFormatted = convertToYearMonth(card.title) // "2024년 12월" -> "2024-12"
                            onReportClick(report.aiReportId, 2, dateFormatted)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // 끝 패딩
        }
    }
}

// "2024년 12월" 형식을 "2024-12" 형식으로 변환하는 함수
private fun convertToYearMonth(displayTitle: String): String {
    return try {
        // "2024년 12월" -> "2024-12"
        val parts = displayTitle.replace("년", "").replace("월", "").trim().split(" ")
        val year = parts[0]
        val month = parts[1].padStart(2, '0') // 한자리수 월을 두자리로 변환
        "$year-$month"
    } catch (e: Exception) {
        // 변환 실패시 현재 년월 반환
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        sdf.format(calendar.time)
    }
}

@Composable
fun ReportCard3DFold(
    yearMonth: String,
    borderColor: Color = Color(0xFF0EA171),
    showRedDot: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(95.dp)
            .zIndex(0f)
            .clickable(
                indication = null, // 클릭 애니메이션 제거
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        // 카드 본체
        Card(
            modifier = Modifier.matchParentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = borderColor.copy(0.1f)),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 1/4 위치 가로선
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height / 4f
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // 오른쪽 아래 접힌 모서리 3D 느낌
                    val foldSize = 20.dp.toPx()
                    val foldColor = borderColor
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width - foldSize, size.height)
                        lineTo(size.width, size.height - foldSize)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = foldColor
                    )
                }

                // 연월 텍스트 (좌측 하단)
                Text(
                    fontFamily = font_gothic_5,
                    text = yearMonth,
                    fontSize = 19.sp,
                    color = Color(0xFF383838),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 12.dp)
                )
            }
        }

        // 빨간 동그라미
        if (showRedDot) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-5).dp)
                    .zIndex(1f)
                    .background(Color(0xFFFD9292), shape = CircleShape)
            )
        }
    }
}



