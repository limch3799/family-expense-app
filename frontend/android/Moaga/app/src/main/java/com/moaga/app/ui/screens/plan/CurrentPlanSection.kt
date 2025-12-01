// file: app/src/main/java/com/moaga/app/ui/screens/plan/CurrentPlanSection.kt
package com.moaga.app.ui.screens.plan

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.moaga.app.data.local.TokenManager
import com.moaga.app.ui.theme.PrimaryColor
import com.moaga.app.ui.theme.moaga_one_bold
import com.moaga.app.ui.theme.moaga_primary_bold
import com.moaga.app.ui.theme.moaga_primary_medium

@Composable
fun CurrentPlanSection(
    primaryGreen: Color,
    titleColor: Color,
    isLoading: Boolean = false,
    planTitle: String = "플랜 정보 없음",
    currentAmount: String = "0",
    targetAmount: String = "0원",
    startDate: String = "날짜 정보 없음",
    dday: String = "D-0",
    progress: Float = 0f
) {
    val cardBg = Color(0xFFEAF6F2)
    val captionColor = Color(0xFF6C7682)
    val context = LocalContext.current

    Spacer(Modifier.height(16.dp))

    // 진행 중인 플랜 상품 텍스트
    Text(
        "   진행 중인 플랜",
        fontSize = 21.sp,
        fontFamily = moaga_one_bold,
        color = PrimaryColor
    )
    Spacer(Modifier.height(4.dp))

    if (isLoading) {
        // 로딩 상태
        Surface(
            color = cardBg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryGreen)
            }
        }
    } else {
        // 첫 번째 Surface (플랜 정보)
        Surface(
            color = cardBg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // TokenManager로 planId 확인 후 PlanActivity로 이동
                    val tokenManager = TokenManager(context)
                    if (tokenManager.hasPlan()) {
                        val intent = Intent(context, PlanActivity::class.java).apply {
                            putExtra("hasPlan", true)
                            putExtra("startHistory", false)
                        }
                        context.startActivity(intent)
                    }
                }
                .zIndex(2f)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 제목과 화살표
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(planTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
                    Text("→", fontSize = 20.sp, color = titleColor)
                }

                Spacer(Modifier.height(8.dp))

                // 저축 금액과 실제 금액을 같은 Row에
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "저축 금액",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                            fontSize = 11.sp,
                            color = captionColor
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(currentAmount, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
                        Spacer(Modifier.width(4.dp))
                        Text("원", fontSize = 16.sp, color = captionColor)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 진행률 프로그래스바
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = primaryGreen,
                    trackColor = Color.White,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 두 번째 Surface (목표 금액 등 정보)
    Surface(
        color = Color(0xFFF1F1F1),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("목표 금액", color = Color(0xFF6C7682))
                Text(targetAmount, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("시작 일자", color = Color(0xFF6C7682))
                Text(startDate, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("금리", color = Color(0xFF6C7682))
                Text("10%", fontWeight = FontWeight.SemiBold, color = primaryGreen)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("남은 기간", color = Color(0xFF6C7682))
                Text(dday, color = primaryGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}