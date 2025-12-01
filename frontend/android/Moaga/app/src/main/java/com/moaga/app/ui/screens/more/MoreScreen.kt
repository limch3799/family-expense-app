// file: app/src/main/java/com/moaga/app/ui/screens/more/MoreScreen.kt
package com.moaga.app.ui.screens.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.moaga.app.R
import com.moaga.app.ui.theme.font_gothic_5

@Composable
fun MoreScreen(navController: NavController) {
    val titleColor = Color(0xFF1E293B)
    val sectionColor = Color(0xFF94A3B8)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "전체 메뉴",
                fontSize = 24.sp,
                fontFamily = font_gothic_5,
                lineHeight = 18.sp,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 계정 관리
        item { SectionHeader("계정 관리", sectionColor) }
        items(listOf("내 정보", "연동 계좌/카드", "가족 그룹 정보", "알림 설정")) { label ->
            val iconRes = when (label) {
                "내 정보" -> R.drawable.user
                "연동 계좌/카드" -> R.drawable.credit_card
                "가족 그룹 정보" -> R.drawable.users
                "알림 설정" -> R.drawable.bell
                else -> R.drawable.more_icon_selected
            }
            MenuItem(label, iconRes) {
                when (label) {
                    "연동 계좌/카드" -> navController.navigate("linked_accounts")
                    "가족 그룹 정보" -> navController.navigate("group_info_detail")
                    "알림 설정" -> navController.navigate("notification_settings")
                    else -> { /* TODO: 다른 메뉴 네비게이션 연결 */ }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // 모든 서비스
        item { SectionHeader("모든 서비스", sectionColor) }
        items(listOf("지출", "가족 지출 리포트", "가족 지출 플랜", "챗봇 금융 퀴즈")) { label ->
            val iconRes = when (label) {
                "지출" -> R.drawable.wallet
                "가족 지출 리포트" -> R.drawable.chart_column
                "가족 지출 플랜" -> R.drawable.calendar
                "챗봇 금융 퀴즈" -> R.drawable.message_square
                else -> R.drawable.more_icon_selected
            }
            MenuItem(label, iconRes) {
                when (label) {
                    "지출" -> navController.navigate("expense_screen")
                    "가족 지출 리포트" -> navController.navigate("analysis")
                    "가족 지출 플랜" -> navController.navigate("plan")
                    "챗봇 금융 퀴즈" -> {
                        // TODO: 챗봇 금융 퀴즈 화면 연결
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // 서비스 정보
        item { SectionHeader("서비스 정보", sectionColor) }
        items(listOf("이용 약관 동의", "고객 센터", "개발 정보")) { label ->
            val iconRes = when (label) {
                "이용 약관 동의" -> R.drawable.file_text
                "고객 센터" -> R.drawable.headset
                "개발 정보" -> R.drawable.code
                else -> R.drawable.more_icon_selected
            }
            MenuItem(label, iconRes) {
                // TODO: 서비스 정보 네비게이션
            }
        }

        item { Spacer(Modifier.height(52.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun MenuItem(label: String, iconRes: Int, onClick: () -> Unit) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111111)
            )
        }
    }
}

/* ------------------- Preview ------------------- */
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MoreScreenPreview() {
    val navController = rememberNavController()
    MoreScreen(navController = navController)
}
