package com.moaga.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.moaga.app.ui.navigation.BottomNavItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CustomBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = BottomNavItem.getAllItems()
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                clip = false
            )
    ) {
        // 바텀 네비게이션 바 배경
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val cornerRadius = with(density) { 25.dp.toPx() }
            val fullPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        topLeftCornerRadius = CornerRadius(cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius)
                    )
                )
            }
            drawPath(path = fullPath, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 2개 (홈, 지출)
            navItems.take(2).forEachIndexed { index, item ->
                CustomNavItem(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // 가운데 공간

            // 오른쪽 2개 (플랜, 전체)
            navItems.drop(3).forEachIndexed { index, item ->
                CustomNavItem(
                    item = item,
                    isSelected = (index + 3) == selectedIndex,
                    onClick = { onItemSelected(index + 3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 가운데 플로팅 버튼
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingNavButton(
                item = navItems[2],
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            Text(
                text = navItems[2].title,
                fontSize = 12.sp,
                color = if (selectedIndex == 2) Color(0xFF04C584) else Color(0xFFC8D2E3),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .offset(y = (-5).dp)
            )
        }
    }
}

@Composable
fun FloatingNavButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var showParticles by remember { mutableStateOf(false) }

    fun playAnimation() {
        coroutineScope.launch {
            showParticles = true
            repeat(2) {
                rotation.animateTo(10f, animationSpec = tween(100))
                rotation.animateTo(-10f, animationSpec = tween(200))
                rotation.animateTo(0f, animationSpec = tween(100))
            }
            delay(1000)
            showParticles = false
        }
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .graphicsLayer(rotationZ = rotation.value)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF33D5A6), Color(0xFF18A87E))
                ),
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                playAnimation()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.icon_report),
            contentDescription = item.title,
            modifier = Modifier.size(42.dp)
        )

        if (showParticles) {
            LightRainAnimation()
        }
    }
}

@Composable
fun LightRainAnimation() {
    val particles = remember { List(25) { RandomParticle() } }
    val transition = rememberInfiniteTransition(label = "rain")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = size.height * progress + p.offsetY
            drawCircle(
                color = Color(0xFF04C584).copy(alpha = 1f - progress),
                radius = 3f,
                center = Offset(
                    x = p.offsetX * size.width,
                    y = y
                )
            )
        }
    }
}

data class RandomParticle(
    val offsetX: Float = Random.nextFloat(),
    val offsetY: Float = Random.nextInt(-20, 0).toFloat()
)

@Composable
fun CustomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF21272A) else Color(0xFFC8D2E3),
        animationSpec = tween(300),
        label = "color"
    )

    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(
                id = when (item.route) {
                    "home" -> if (isSelected) R.drawable.home_icon_selected else R.drawable.home_icon_unselected
                    "expense" -> if (isSelected) R.drawable.expense_icon_selected else R.drawable.expense_icon_unselected
                    "plan" -> if (isSelected) R.drawable.plan_icon_selected else R.drawable.plan_icon_unselected
                    "more" -> if (isSelected) R.drawable.more_icon_selected else R.drawable.more_icon_unselected
                    else -> if (isSelected) R.drawable.home_icon_selected else R.drawable.home_icon_unselected
                }
            ),
            contentDescription = item.title,
            modifier = Modifier.size(width = 36.dp, height = 52.dp)
        )
    }
}
