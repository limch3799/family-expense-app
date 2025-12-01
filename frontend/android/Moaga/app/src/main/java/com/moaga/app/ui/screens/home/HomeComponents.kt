package com.moaga.app.ui.screens.home

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.moaga.app.R
import com.moaga.app.data.local.TokenManager
import com.moaga.app.ui.components.home.TodayExpenseCard
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_6
import com.moaga.app.ui.theme.moaga_two_regular

// 사용자 이름에 따른 프로필 이미지 반환 함수
fun getUserProfileImage(username: String): Int {
    return when (username) {
        "김아빠" -> R.drawable.profile_character_purple
        "신엄마" -> R.drawable.profile_character_pink
        "진아들" -> R.drawable.profile_character_orange
        "동그라미" -> R.drawable.profile_character_green
        else -> R.drawable.profile_character_purple // 기본값은 보라색
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeHeader(
    username: String,
    uiState: HomeUiState,
    homeViewModel: HomeViewModel,
    navController: NavController? = null // NavController를 옵셔널로 만들어 호환성 확보
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserName = tokenManager.getUsername() ?: username

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .zIndex(-1f)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 새로운 레이아웃: Row(왼쪽 컬럼, 오른쪽 컬럼)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 컬럼: 프로필 이미지 + 닉네임 + 님
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = getUserProfileImage(currentUserName)),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }

                    Text(
                        text = currentUserName,
                        fontFamily = font_paperlogy_6,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(start = 6.dp)
                    )
                    Text(
                        text = "님",
                        fontFamily = moaga_two_regular,
                        fontSize = 21.sp,
                        color = Color.White,
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(start = 4.dp)
                    )
                }
            }

            // 오른쪽 컬럼: 알림 아이콘 + 더보기 아이콘
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_notification_on),
                        contentDescription = "Notification",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showLocalNotification(
                                    context = context,
                                    title = "🎉 새로운 플랜이 개설되었습니다!",
                                    body = "가족 적금 플랜이 성공적으로 개설되었어요.\n지금부터 모든 그룹원이 함께 참여할 수 있습니다."
                                )
                                // NavController가 있으면 네비게이션, 없으면 기본 클릭 처리
                                navController?.navigate("notification_settings")
                            }
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.icon_more),
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // 더보기 클릭 처리
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TodayExpenseCard - 두 번째 파일의 개선된 로딩 처리 방식 적용
        uiState.todayExpenseData?.let { expenseData ->
            TodayExpenseCard(
                familyName = expenseData.familyName,
                members = expenseData.members,
                updatedAt = expenseData.updatedAt,
                // 첫 번째 파일의 숨김 기능과 두 번째 파일의 일반 표시를 통합
                totalAmount = if (expenseData.isHidden) " " else expenseData.totalAmount,
                isLoading = uiState.isTodayExpenseLoading, // 두 번째 파일에서 가져온 개선사항
                onRefreshClick = { homeViewModel.refreshTodayExpense() },
                onToggleVisibility = { homeViewModel.toggleAmountVisibility() }
            )
        } ?: run {
            // 데이터가 없을 때 - 두 번째 파일의 개선된 방식 적용
            // 로딩 중일 때만 로티 애니메이션이 포함된 카드 표시
            if (uiState.isLoading || uiState.isTodayExpenseLoading) {
                TodayExpenseCard(
                    familyName = "",
                    members = emptyList(),
                    updatedAt = "",
                    totalAmount = "0",
                    isLoading = true,
                    onRefreshClick = { homeViewModel.refreshTodayExpense() },
                    onToggleVisibility = { homeViewModel.toggleAmountVisibility() }
                )
            } else {
                // 첫 번째 파일의 CircularProgressIndicator 방식을 fallback으로 유지
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BoxScope.HomeFloatingButton(
    showFab: Boolean,
    navController: NavController
) {
    var isFabExpanded by remember { mutableStateOf(false) }

    // 스크롤이 시작되면 FAB 축소
    LaunchedEffect(showFab) {
        if (!showFab && isFabExpanded) {
            isFabExpanded = false
        }
    }

    // 배경 오버레이
    AnimatedVisibility(
        visible = isFabExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { isFabExpanded = false }
        )
    }

    // 플로팅 버튼
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
                    fontFamily = font_gothic_5,
                    modifier = Modifier.clickable {  // 이 부분 추가
                        navController.navigate("linked_accounts")
                        isFabExpanded = false  // FAB 닫기
                    }
                )
            }

            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                containerColor = if (isFabExpanded) Color.White else Color(0xFF767676),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                if (isFabExpanded) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "FAB",
                        tint = Color.Black
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_home_floating),
                        contentDescription = "FAB",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

fun showLocalNotification(context: Context, title: String, body: String) {
    val channelId = "local_test_channel"

    // Android 8.0 이상은 채널 등록 필요
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "테스트 알림 채널",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.moagom)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification) // 단색 벡터
        .setLargeIcon(largeIcon)                  // 알림창 안에 컬러 표시
        .setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(largeIcon)
                .bigLargeIcon(null as Bitmap?)
        )
        .setContentTitle("플랜 생성 완료 🎉")
        .setContentText("가족 저축 플랜이 개설되었습니다!")
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    // 🚨 알림 권한 확인 (Android 13 이상)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    } else {
        // 권한이 없을 경우 처리 (로그 찍기 등)
        android.util.Log.w("Notification", "POST_NOTIFICATIONS 권한 없음")
    }
}