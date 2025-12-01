// file: app/src/main/java/com/moaga/app/ui/screens/group/create/IdInfoConfirmScreen.kt
package com.moaga.app.ui.screens.group.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdInfoConfirmScreen(
    name: String,
    rrn: String,
    issue: String,
    onBack: () -> Unit = {},
    onYes: () -> Unit = {},
    onNo: () -> Unit = {}
) {
    val inputBg   = Color(0xFFEAF6F2)
    val brandBlue = Color(0xFF18A87E)
    val denyRed   = Color(0xFFFF8C8C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("신분증 확인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
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

            LabeledReadOnlyField("이름", name, inputBg)
            LabeledReadOnlyField("주민등록번호", rrn, inputBg)
            LabeledReadOnlyField("발급일자", issue, inputBg)

            Spacer(Modifier.height(8.dp))
            Text("해당 정보가 맞습니까?", fontSize = 18.sp)

            Button(
                onClick = onYes,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) { Text("네", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = onNo,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = denyRed)
            ) { Text("아니요", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun LabeledReadOnlyField(label: String, value: String, bg: Color) {
    Text(label, fontSize = 14.sp, color = Color(0xFF111111))
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = bg,
            disabledTextColor = Color(0xFF333333),
            disabledBorderColor = Color.Transparent,
            disabledLabelColor = Color.Unspecified,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}
