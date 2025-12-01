package com.moaga.app.ui.components.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.moaga.app.R
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.local.TokenManager
import com.moaga.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun TodayExpenseCard(
    familyName: String,
    members: List<String>,
    updatedAt: String,
    totalAmount: String,
    isLoading: Boolean = false,
    onRefreshClick: () -> Unit = {},
    onToggleVisibility: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var isRotating by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
    )

    // TokenManager에서 그룹 멤버 목록 가져오기
    val tokenManager = remember { TokenManager(ctx) }
    val groupMembers = remember { tokenManager.getGroupMembers() }

    // ✅ updatedAt 상태로 관리
    var internalUpdatedAt by remember { mutableStateOf(updatedAt) }
    var displayTime by remember { mutableStateOf("--") }

    // 초기 로딩 시 한번 불러오기
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getGroupLastUpdated()
            if (res.isSuccessful) {
                val time = res.body()?.lastUpdated ?: "--:--:--"
                internalUpdatedAt = time
                displayTime = formatRelativeTime(time)
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "업데이트 시간 조회 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // updatedAt prop이 변경될 때도 업데이트
    LaunchedEffect(updatedAt) {
        internalUpdatedAt = updatedAt
        displayTime = formatRelativeTime(updatedAt)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽 Column: 오늘의 우리 지출 + 멤버
                Column {
                    Text(
                        text = "오늘의 총 지출",
                        fontSize = 21.sp,
                        fontFamily = font_paperlogy_7,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 21.sp,
                        color = Color(0xFF000000)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "멤버:",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = font_paperlogy_5
                        )
                        Text(
                            text = groupMembers.map { it.username }.joinToString(", "),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 10.sp,
                            fontFamily = font_paperlogy_5
                        )
                    }
                }

                // 오른쪽 Column: 가족명 + 날짜시간
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF6EEA0),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = familyName,
                            fontSize = 12.sp,
                            fontFamily = font_sketch,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayTime,
                            fontSize = 10.sp,
                            fontFamily = font_paperlogy_7
                        )

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(rotationZ = rotation % 360) // ✅ 회전 적용
                                .noRippleClickable {
                                    scope.launch {
                                        rotationTarget += 720f
                                        isRotating = true
                                        try {
                                            val res = ApiClient.apiService.syncTransactions()
                                            if (res.isSuccessful) {
                                                val timeRes = ApiClient.apiService.getGroupLastUpdated()
                                                if (timeRes.isSuccessful) {
                                                    val time = timeRes.body()?.lastUpdated ?: internalUpdatedAt
                                                    internalUpdatedAt = time
                                                    displayTime = formatRelativeTime(time)
                                                }
                                                onRefreshClick() // 외부 콜백 호출
                                            } else {
                                                Toast.makeText(ctx, "실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isRotating = false
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "숨김",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = moaga_primary_medium,
                    lineHeight = 10.sp,
                    modifier = Modifier
                        .alignByBaseline()
                        .background(
                            color = Color(0xFFD9D9D9),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggleVisibility() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 로딩 중일 때 로티 애니메이션, 회전 중일 때도 로딩, 아니면 금액 표시
                Box(
                    modifier = Modifier.height(38.dp), // ✅ 고정 높이
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading || isRotating) {
                        LoadingAnimation()
                    } else {
                        WaveAmountText(totalAmount = totalAmount)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 구분선
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFDDDDDD))
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 멤버 리스트 (LazyRow로 스크롤 가능하게 변경)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(groupMembers) { member ->
                    MemberItemBox(
                        imageRes = getMemberProfileImage(member.username),
                        username = member.username
                    )
                }
            }
        }
    }
}

// 멤버 이름에 따른 프로필 이미지 반환 함수
fun getMemberProfileImage(username: String): Int {
    return when (username) {
        "김아빠" -> R.drawable.profile_character_purple
        "신엄마" -> R.drawable.profile_character_pink
        "진아들" -> R.drawable.profile_character_orange
        "동그라미" -> R.drawable.profile_character_green
        else -> R.drawable.profile_character_purple // 기본값은 보라색
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_block))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(60.dp)
    )
}

@Composable
fun WaveAmountText(totalAmount: String) {
    var startAnimation by remember { mutableStateOf(false) }

    // 컴포넌트가 처음 로드될 때 애니메이션 시작
    LaunchedEffect(totalAmount) {
        startAnimation = true
    }

    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "-",
            fontSize = 38.sp,
            fontFamily = moaga_primary_bold,
            color = Color.Black,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = " ",
            fontSize = 10.sp,
            fontFamily = moaga_primary_bold,
            color = Color.Black,
            modifier = Modifier.alignByBaseline()
        )
        // 각 자릿수별로 애니메이션 적용
        totalAmount.forEachIndexed { index, char ->
            AnimatedDigit(
                char = char.toString(),
                delay = index * 80L, // 각 자릿수마다 80ms 지연
                startAnimation = startAnimation,
                fontSize = 38.sp,
                fontFamily = moaga_primary_bold,
                color = Color.Black,
                modifier = Modifier.alignByBaseline()
            )
        }

        Text(
            text = " 원",
            fontSize = 24.sp,
            color = Color(0xFFACACAC),
            fontFamily = font_paperlogy_6,
            modifier = Modifier.alignByBaseline()
        )
    }
}

@Composable
fun AnimatedDigit(
    char: String,
    delay: Long,
    startAnimation: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animationState by remember { mutableStateOf(false) }

    // 지연 후 애니메이션 시작
    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            delay(delay)
            animationState = true
        }
    }

    // 파도 효과 애니메이션
    val animatedY by animateFloatAsState(
        targetValue = if (animationState) 0f else -8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wave_animation"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationState) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseOutCubic
        ),
        label = "fade_animation"
    )

    Text(
        text = char,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = color,
        modifier = modifier.graphicsLayer(
            translationY = animatedY,
            alpha = animatedAlpha
        )
    )
}

@Composable
fun MemberItemBox(imageRes: Int, username: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(18.dp) // 절반 크기
                    .clip(CircleShape)
            )

            Text(
                text = username,
                fontFamily = font_paperlogy_6,
                fontSize = 16.sp, // 절반 크기
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .alignByBaseline()
                    .padding(start = 4.dp)
            )
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    )
}

fun formatRelativeTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul") // ✅ 한국 시간대 적용
        val parsedDate = sdf.parse(timestamp) ?: return timestamp

        val diffMillis = System.currentTimeMillis() - parsedDate.time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)

        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(parsedDate)
        }
    } catch (e: Exception) {
        timestamp // 실패하면 원본 반환
    }
}