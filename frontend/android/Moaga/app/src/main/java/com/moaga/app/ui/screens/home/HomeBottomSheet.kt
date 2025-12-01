package com.moaga.app.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moaga.app.R
import com.moaga.app.ui.components.home.FamilyDepositCard
import com.moaga.app.ui.components.home.FamilySpendingChangeCard
import com.moaga.app.ui.components.home.SavingPlanCard
import com.moaga.app.ui.theme.font_paperlogy_6

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeBottomSheet(
    scrollState: LazyListState,
    whiteBoxTopPadding: Dp,
    uiState: HomeUiState,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onNavigateToPlan: () -> Unit // 플랜 탭으로 이동하는 콜백 추가
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = whiteBoxTopPadding)
            .background(
                Color(0xFFF6F6F6),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clipToBounds()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.home_vector_1),
                contentDescription = "Home Vector",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.height(20.dp)
            )
        }

        // 카드들
        item {
            Column {
                // 지출 추이 카드
                uiState.spendingTrendData?.let { trendData ->
                    FamilySpendingChangeCard(
                        navController = navController,
                        todayAmount = uiState.formattedTodayAmount,
                        chartData = trendData.chartData,
                        onRefresh = { homeViewModel.refreshSpendingTrend() }
                    )
                } ?: run {
                    FamilySpendingChangeCard(
                        navController = navController,
                        onRefresh = { homeViewModel.refreshSpendingTrend() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 가족 예금 카드
                uiState.familyDepositData?.let { depositData ->
                    FamilyDepositCard(
                        savingAccountNo = depositData.savingAccountNo,
                        amount = depositData.amount,
                        isAmountHidden = depositData.isAmountHidden,
                        onToggleVisibility = { homeViewModel.toggleDepositAmountVisibility() },
                        onClick = { navController.navigate("family_deposit_info") }
                    )
                } ?: run {
                    FamilyDepositCard(
                        onClick = { navController.navigate("family_deposit_info") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 저축 플랜 카드 - uiState에서 플랜 데이터 가져오기
                SavingPlanCard(
                    planData = uiState.currentPlanData,  // uiState에서 가져옴
                    isLoading = uiState.isPlanLoading,   // uiState에서 가져옴
                    onCardClick = {
                        onNavigateToPlan() // 플랜 탭으로 이동
                    }
                )
            }
        }

        // 나머지 카드들
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            navController.navigate("group_info_detail")
                        },
                    //colors = CardDefaults.cardColors(containerColor = Color(0xFFF6ECFC)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // elevation 추가
                    border = BorderStroke(1.dp, Color(0xFFE5E5E5)) // 보더 추가
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_character_family),
                            contentDescription = "Family Icon",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "우리 가족 그룹",
                            fontSize = 18.sp,
                            fontFamily = font_paperlogy_6,
                            //color = Color(0xFFAF4AEE),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    //colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF4EA)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // elevation 추가
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)) // 보더 추가
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                navController.navigate("my_expense")
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_home_card),
                            contentDescription = "Card Icon",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "나의 지출 내역",
                            fontSize = 18.sp,
                            fontFamily = font_paperlogy_6,
                            //color = Color(0xFFF6A225),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 퀴즈 카드
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD0EDFC)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // elevation 추가
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)) // 보더 추가
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.character_quiz),
                        contentDescription = "Card Icon",
                        modifier = Modifier.size(140.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "오늘의 금융 퀴즈",
                        fontSize = 18.sp,
                        fontFamily = font_paperlogy_6,
                        //color = Color(0xFF0788C6),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(86.dp))
        }
    }
}