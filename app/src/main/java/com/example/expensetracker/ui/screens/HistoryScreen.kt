package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit // For potential edit action
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.database.entity.Expense
import com.example.expensetracker.ui.viewmodel.HistoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    // navController: NavController // Add later for navigating to edit screen
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showFilterDialog by remember { mutableStateOf(false) } // State for filter dialog

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense History") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter Expenses")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // TODO: Implement Filter Dialog UI triggered by showFilterDialog state

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.expenses.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No expenses recorded yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.expenses, key = { it.id }) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            categoryName = uiState.categories.find { it.id == expense.categoryId }?.name ?: "Unknown",
                            numberFormat = numberFormat,
                            dateFormatter = dateFormatter,
                            onEditClick = { /* TODO: Navigate to edit screen */ }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // Placeholder for Filter Dialog - Implement later
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Expenses") },
            text = { Text("Filter options (Date Range, Category) will go here.") },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) { Text("Apply") }
            },
            dismissButton = {
                 Button(onClick = {
                     viewModel.clearFilters()
                     showFilterDialog = false
                 }) { Text("Clear") }
            }
        )
    }
}

@Composable
fun ExpenseListItem(
    expense: Expense,
    categoryName: String,
    numberFormat: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(categoryName, fontWeight = FontWeight.Bold) },
        supportingContent = {
            Column {
                Text(dateFormatter.format(expense.date))
                expense.notes?.let { Text(it) }
            }
        },
        trailingContent = {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Text(numberFormat.format(expense.amount), style = MaterialTheme.typography.bodyLarge)
                 Spacer(modifier = Modifier.width(8.dp))
                 IconButton(onClick = onEditClick) {
                     Icon(Icons.Default.Edit, contentDescription = "Edit Expense")
                 }
             }
        },
        modifier = modifier
    )
}