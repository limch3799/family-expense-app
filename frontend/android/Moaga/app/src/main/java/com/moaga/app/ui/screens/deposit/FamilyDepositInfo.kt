package com.moaga.app.ui.screens.deposit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.components.deposit.DepositAccountCard
import com.moaga.app.ui.components.deposit.DepositInfoCard
import com.moaga.app.ui.components.deposit.TransactionItem
import com.moaga.app.ui.theme.font_gothic_5

data class Transaction(
    val dateTime: String,
    val userName: String,
    val amount: String
)

@Composable
@Preview
fun FamilyDepositInfo() {

    val transactions = listOf(
        Transaction("2024.01.15 14:30:25", "임창현", "+120,000원"),
        Transaction("2024.01.12 09:15:10", "동현진", "+50,000원"),
        Transaction("2024.01.10 16:45:30", "임창현", "-30,000원"),
        Transaction("2024.01.08 11:20:15", "동현진", "+200,000원"),
        Transaction("2024.01.05 13:10:45", "임창현", "+75,000원")
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 고정된 상단 헤더 (뒤로가기 버튼 + 제목)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* 뒤로가기 로직 */ }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.Black
                )
            }

            Text(
                text = "가족 예금 정보",
                fontSize = 18.sp,
                fontFamily = font_gothic_5,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // 오른쪽 공간 균형을 위한 빈 공간
            Spacer(modifier = Modifier.width(48.dp))
        }

        // 스크롤 가능한 콘텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 계좌번호와 잔액 카드
            DepositAccountCard()

            Spacer(modifier = Modifier.height(16.dp))

            // 송금하기 버튼
            Button(
                onClick = { /* 송금 로직 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6CB39E)
                )
            ) {
                Text(
                    text = "송금하기",
                    fontSize = 16.sp,
                    fontFamily = font_gothic_5,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 예금정보 텍스트
            Text(
                text = "예금정보",
                fontSize = 16.sp,
                fontFamily = font_gothic_5,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 예금정보 카드
            DepositInfoCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 거래내역 텍스트
            Text(
                text = "거래내역",
                fontSize = 16.sp,
                fontFamily = font_gothic_5,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 거래내역 아이템들을 Column으로 변경 (LazyColumn 대신)
            transactions.forEach { transaction ->
                TransactionItem(transaction = transaction)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 하단 여백 추가
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}