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
// Base class for screens with icons (typically for bottom nav)
sealed class ScreenWithIcon(route: String, label: String, val icon: ImageVector) : Screen(route, label) {
    object Overview : ScreenWithIcon("overview", "Overview", Icons.Filled.PieChart)
    object History : ScreenWithIcon("history", "History", Icons.Filled.History)
    object Budgets : ScreenWithIcon("budgets", "Budgets", Icons.Filled.AccountBalanceWallet)
    object Settings : ScreenWithIcon("settings", "Settings", Icons.Filled.Settings)
}

// Base class for all screens
sealed class Screen(val route: String, val label: String) {
    // Reference bottom nav screens
    object Overview : Screen(ScreenWithIcon.Overview.route, ScreenWithIcon.Overview.label)
    object History : Screen(ScreenWithIcon.History.route, ScreenWithIcon.History.label)
    object Budgets : Screen(ScreenWithIcon.Budgets.route, ScreenWithIcon.Budgets.label)
    object Settings : Screen(ScreenWithIcon.Settings.route, ScreenWithIcon.Settings.label)

    // Add/Edit Expense Screen
    object AddEditExpense : Screen("add_edit_expense?expenseId={expenseId}", "Add/Edit Expense") {
        // Helper function to create the route for adding (no ID)
        fun createRoute() = "add_edit_expense"
        // Helper function to create the route for editing (with ID)
        fun createRoute(expenseId: Long) = "add_edit_expense?expenseId=$expenseId"
        const val expenseIdArg = "expenseId" // Argument name
    }

    // Category Management Screen
    object CategoryManagement : Screen("category_management", "Manage Categories")
}

// List of screens to appear in the bottom navigation bar
// List of screens to appear in the bottom navigation bar
val bottomNavItems = listOf(
    ScreenWithIcon.Overview,
    ScreenWithIcon.History,
    ScreenWithIcon.Budgets,
    ScreenWithIcon.Settings
)