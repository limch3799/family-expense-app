// file: app/src/main/java/com/moaga/app/ui/screens/group/NoGroupScreen.kt
package com.moaga.app.ui.screens.group

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.moaga.app.R
import com.moaga.app.ui.screens.group.GroupActivity

@Composable
fun NoGroupScreen(
    onBackToLogin: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val brandGreen = Color(0xFF18A87E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.moaga_logo_color),
                contentDescription = "로고",
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(40.dp))

            Text("가족 그룹에 참여해보세요!", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(40.dp))

            PrimaryBigButton(
                text = "가족 그룹 생성하기",
                leading = { Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(36.dp)) },
                onClick = {
                    ctx.startActivity(Intent(ctx, GroupActivity::class.java).putExtra("flow", "create"))
                },
                container = brandGreen
            )
            Spacer(Modifier.height(20.dp))
            PrimaryBigButton(
                text = "가족 그룹 참여하기",
                leading = { Icon(Icons.Outlined.Group, contentDescription = null, modifier = Modifier.size(36.dp)) },
                onClick = {
                    ctx.startActivity(Intent(ctx, GroupActivity::class.java).putExtra("flow", "join"))
                },
                container = brandGreen
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PrimaryBigButton(
    text: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    container: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            leading()
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
