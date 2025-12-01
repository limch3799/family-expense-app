package com.moaga.app.ui.components.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.screens.expense.ExpenseItem
import com.moaga.app.ui.theme.font_gothic_3
import com.moaga.app.ui.theme.font_gothic_4
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.moaga_primary_bold
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.moaga.app.R
import com.moaga.app.ui.theme.*

/**
 * 카테고리별 배경색을 순서대로 반환하는 함수
 * 빨간색 -> 초록색 -> 파란색 -> 보라색 -> 주황색 순서로 순환
 */
private fun getCategoryBackgroundColor(category: String): Color {
    return when (category) {
        // 🍽️ 식음료/생활소비
        "외식", "배달", "카페/간식", "술/주점" -> Color(0xFFE57373) // 빨강
        "마트/편의점", "대형마트", "생활", "쇼핑", "온라인 쇼핑" -> Color(0xFFFFB74D) // 주황

        // 🚇 교통/여행/주거/통신
        "교통", "여행/숙박" -> Color(0xFF64B5F6) // 파랑
        "주거/통신", "통신" -> Color(0xFF4DB6AC) // 청록

        // 💰 금융/투자/세금
        "저축/투자", "대출", "카드대금", "세금" -> Color(0xFFA1887F) // 갈색
        "보험", "ATM", "이체" -> Color(0xFF9E9E9E) // 회색

        // 👨‍👩‍👧 교육/자녀/건강
        "교육", "교육/육아", "자녀" -> Color(0xFF81C784) // 연두
        "병원/건강" -> Color(0xFFBA68C8) // 보라
        "생활편의" -> Color(0xFFF06292) // 핑크

        // 🎉 사회적 지출
        "기부/후원", "경조/선물", "모임" -> Color(0xFF9575CD) // 진한 보라

        // 🌍 기타
        "미분류" -> Color(0xFFBDBDBD) // 회색
        "해외", "기타" -> Color(0xFFFFEE58)

        else -> Color(0xFFBDBDBD) // 기본값 회색
    }
}

/**
 * 사람 이름에 따라 프로필 이미지를 순서대로 반환하는 함수
 * purple -> green -> orange -> pink 순서로 순환
 */
// 프로필 이미지 순서 카운터
private val profileImageMap = mutableMapOf<String, Int>()
private var nextProfileIndex = 0

fun getProfileImage(personName: String): Int {
    val profileImages = listOf(
        R.drawable.profile_character_purple,
        R.drawable.profile_character_green,
        R.drawable.profile_character_orange,
        R.drawable.profile_character_pink
    )

    return profileImageMap.getOrPut(personName) {
        val assignedImage = profileImages[nextProfileIndex]
        nextProfileIndex = (nextProfileIndex + 1) % profileImages.size
        assignedImage
    }
}

@Composable
fun ExpenseItemComponent(
    item: ExpenseItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 날짜 + 시간 + (프로필 이미지 + 사람이름)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.date} ${item.time}",
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 프로필 이미지 + 이름
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = getProfileImage(item.person)),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = item.person,
                            fontSize = 12.sp,
                            color = Color(0xFF1A1A1A),
                            fontFamily = font_gothic_5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 카테고리 텍스트 - 색상 함수 적용
                Text(
                    text = item.category,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = Color.White,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            color = getCategoryBackgroundColor(item.category),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 두 번째 줄 (이름, 금액)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    fontFamily = font_gothic_4
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.amount,
                        fontSize = 14.sp,
                        color = if (item.isExcluded) Color.Gray else Color.Black, // ✅ 제외 시 회색 처리
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.isExcluded) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(제외)", // ✅ 표시
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontFamily = font_gothic_3
                        )
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color(0xFFE0E0E0)
        )
    }
}