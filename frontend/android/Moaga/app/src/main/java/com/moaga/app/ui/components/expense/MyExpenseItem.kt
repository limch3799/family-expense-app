package com.moaga.app.ui.components.expense

import androidx.compose.foundation.background
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
import com.moaga.app.ui.screens.expense.MyExpenseItem
import com.moaga.app.ui.theme.font_gothic_3
import com.moaga.app.ui.theme.font_gothic_4
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.moaga_primary_bold

@Composable
fun MyExpenseItemComponent(
    item: MyExpenseItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 지출 아이템 내용
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // 첫 번째 줄: 날짜+시간, 카테고리
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 날짜 + 시간
                Text(
                    text = "${item.date} ${item.time}",
                    fontSize = 10.sp,
                    color = Color(0xFF666666),
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Medium
                )

                // 카테고리 (배경 있는 텍스트)
                Text(
                    text = item.category,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = Color.White,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            color = when (item.category) {
                                "식비" -> Color(0xFF60E066)
                                "교통" -> Color(0xFF82BCEA)
                                "쇼핑" -> Color(0xFFD869EA)
                                "카페" -> Color(0xFFE79679)
                                else -> Color(0xFFEF7A7A)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 두 번째 줄: 이름, 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이름
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    fontFamily = font_gothic_4
                )

                // 금액
                Text(
                    text = item.amount,
                    fontSize = 14.sp,
                    color = Color(0xFF000000),
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 구분선
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color(0xFFE0E0E0)
        )
    }
}