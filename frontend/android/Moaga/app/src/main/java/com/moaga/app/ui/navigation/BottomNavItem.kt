package com.moaga.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String // 네비게이션 라우트 추가
) {
    data object Home : BottomNavItem("", Icons.Default.Home, "home")
    data object Expense : BottomNavItem("", Icons.Default.CreditCard, "expense")
    data object Analysis : BottomNavItem("", Icons.Default.Analytics, "analysis")
    data object Plan : BottomNavItem("", Icons.Default.Savings, "plan")
    data object More : BottomNavItem("", Icons.Default.Menu, "more")

    companion object {
        fun getAllItems() = listOf(Home, Expense, Analysis, Plan, More)

        fun getItemByRoute(route: String?): BottomNavItem? {
            return getAllItems().find { it.route == route }
        }
    }
}