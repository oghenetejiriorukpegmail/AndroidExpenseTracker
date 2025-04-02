package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar // Use NavigationBar for Material 3
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.ui.navigation.Screen
import com.example.expensetracker.ui.navigation.ScreenWithIcon // Import the correct type for bottomNavItems
import com.example.expensetracker.ui.navigation.bottomNavItems
import com.example.expensetracker.ui.screens.AddEditExpenseScreen // Import the new screen
import com.example.expensetracker.ui.screens.BudgetsScreen
import com.example.expensetracker.ui.screens.HistoryScreen
import com.example.expensetracker.ui.screens.OverviewScreen
import com.example.expensetracker.ui.screens.SettingsScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar { // Material 3 Bottom Navigation
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen: ScreenWithIcon -> // Specify type
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // Show FAB only on certain screens if needed (e.g., not on Add/Edit itself)
            // val currentRoute = navBackStackEntry?.destination?.route
            // if (currentRoute == Screen.Overview.route || currentRoute == Screen.History.route) {
                FloatingActionButton(onClick = {
                    // Navigate to Add screen (no expenseId)
                    navController.navigate(Screen.AddEditExpense.createRoute())
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Expense")
                }
            // }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Overview.route,
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(Screen.Overview.route) { OverviewScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Budgets.route) { BudgetsScreen() }
            composable(Screen.Settings.route) { SettingsScreen(/* Pass navController if needed later */) }

            // Add/Edit Expense Screen Route
            composable(
                route = Screen.AddEditExpense.route,
                arguments = listOf(
                    navArgument(Screen.AddEditExpense.expenseIdArg) {
                        type = NavType.LongType
                        defaultValue = -1L // Use -1 to indicate "add mode"
                    }
                )
            ) { backStackEntry ->
                // val expenseId = backStackEntry.arguments?.getLong(Screen.AddEditExpense.expenseIdArg)
                // Pass expenseId to ViewModel later if needed for editing
                AddEditExpenseScreen(
                    // navController = navController // Pass navController for navigating back
                )
            }
        }
    }
}

// We can add a preview for the main app structure later if needed
// @Preview(showBackground = true)
// @Composable
// fun ExpenseTrackerAppPreview() {
//     ExpenseTrackerTheme {
//         ExpenseTrackerApp()
//     }
// }