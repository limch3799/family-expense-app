package com.moaga.app.ui.components.deposit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.ui.theme.font_gothic_5
import com.moaga.app.ui.theme.font_paperlogy_5
import com.moaga.app.ui.theme.font_paperlogy_6
import com.moaga.app.ui.theme.moaga_primary_bold
import com.moaga.app.ui.theme.moaga_primary_medium

@Composable
@Preview
fun DepositAccountCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(1.5.dp, Color(0xFFECECEC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 계좌번호 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "계좌번호 ",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = font_paperlogy_5
                    )
                    Text(
                        text = "123-2354-3451-11",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = font_paperlogy_5,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "복사",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            // 복사 로직
                        },
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 잔액 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
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
                        .clickable {
                            // 숨김 로직
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "9,300,000",
                    fontSize = 32.sp,
                    fontFamily = moaga_primary_bold,
                    color = Color.Black,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = " 원",
                    fontSize = 20.sp,
                    color = Color(0xFFACACAC),
                    fontFamily = font_paperlogy_6,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}