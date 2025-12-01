package com.moaga.app.ui.screens.group.create

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.GroupCreateRequest
import com.moaga.app.ui.theme.MoagaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    onBack: () -> Unit = {},
    onNext: (name: String, description: String) -> Unit = { _, _ -> }
) {
    val inputBg = Color(0xFFEAF6F2)
    val brandBlue = Color(0xFF18A87E)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "가족그룹 생성",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 그룹명
            Text("그룹명", fontSize = 14.sp, color = Color(0xFF111111))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(15) }, // 최대 15자
                placeholder = { Text("그룹명을 입력해주세요. (최대 15자)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            // 그룹 설명 (선택)
            Text("그룹 설명 (선택)", fontSize = 14.sp, color = Color(0xFF111111))
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it.take(30) }, // 최대 30자
                placeholder = { Text("그룹 설명을 입력해주세요. (최대 30자)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { scope.launch {
                    loading = true
                    try {
//                        val res = ApiClient.apiService.createGroup(
//                            GroupCreateRequest(
//                                name = name.trim(),
//                                description = desc.trim()
//                            )
//                        )
                        val resSuccessful  = true
                        if (resSuccessful) {
                            onNext(name.trim(), desc.trim()) // 기존 플로우 유지
                        } else {
//                            Toast.makeText(ctx, "그룹 생성 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        val msg = "네트워크 오류: ${e.localizedMessage}"
                        Log.e("GroupCreateScreen", msg, e)
                        Toast.makeText(ctx, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        loading = false
                    }
                } },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) {
                Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/* Preview */
@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GroupCreateScreenPreview() {
    MoagaTheme {
        GroupCreateScreen()
    }
}
