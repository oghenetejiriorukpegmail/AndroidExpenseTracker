package com.example.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines the possible navigation routes within the app.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Overview : Screen("overview", "Overview", Icons.Filled.PieChart)
    object History : Screen("history", "History", Icons.Filled.History)
    object Budgets : Screen("budgets", "Budgets", Icons.Filled.AccountBalanceWallet)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    // Add other screens like Add/Edit Expense later, which might not be in the bottom bar
}

// List of screens to appear in the bottom navigation bar
val bottomNavItems = listOf(
    Screen.Overview,
    Screen.History,
    Screen.Budgets,
    Screen.Settings
)