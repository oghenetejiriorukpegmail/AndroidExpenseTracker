package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.database.entity.Budget
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.ui.viewmodel.BudgetDisplayData
import com.example.expensetracker.ui.viewmodel.BudgetViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddEditDialog by remember { mutableStateOf(false) }
    // Use budgetToEdit from uiState to control edit dialog visibility and prefill data
    val budgetToEdit = uiState.budgetToEdit

    // Trigger dialog open when budgetToEdit state changes
    LaunchedEffect(budgetToEdit) {
        if (budgetToEdit != null) {
            showAddEditDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Budgets (${uiState.currentMonth}/${uiState.currentYear})") })
            // TODO: Add month/year selector later
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.setBudgetToEdit(null) // Ensure we are in "add" mode
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "An error occurred",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (uiState.budgetsWithProgress.isEmpty()) {
                Text(
                    text = "No budgets set for this month. Add one using the '+' button.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.budgetsWithProgress, key = { it.budget.id }) { budgetData ->
                        BudgetListItem(
                            budgetData = budgetData,
                            onEditClick = { viewModel.setBudgetToEdit(budgetData.budget) },
                            onDeleteClick = { viewModel.deleteBudget(budgetData.budget) }
                        )
                    }
                }
            }

            // Add/Edit Budget Dialog
            if (showAddEditDialog) {
                BudgetEditDialog(
                    dialogTitle = if (budgetToEdit == null) "Add Budget" else "Edit Budget",
                    initialBudget = budgetToEdit,
                    availableCategories = uiState.availableCategories,
                    onDismiss = {
                        showAddEditDialog = false
                        viewModel.setBudgetToEdit(null) // Clear edit state on dismiss
                    },
                    onConfirm = { amount, categoryId ->
                        viewModel.addOrUpdateBudget(amount, categoryId)
                        showAddEditDialog = false
                        // ViewModel handles clearing edit state on success
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetListItem(
    budgetData: BudgetDisplayData,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val progressColor = when {
        budgetData.progress > 1f -> Color.Red // Over budget
        budgetData.progress > 0.8f -> Color(0xFFFFA500) // Orange - nearing budget
        else -> MaterialTheme.colorScheme.primary
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = budgetData.categoryName ?: "Overall Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                     IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Budget")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Budget")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Spent: ${numberFormat.format(budgetData.totalSpent)} / ${numberFormat.format(budgetData.budget.amount)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { budgetData.progress.coerceIn(0f, 1f) }, // Use coerced progress for visual bar
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditDialog(
    dialogTitle: String,
    initialBudget: Budget?,
    availableCategories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (amount: BigDecimal, categoryId: Long?) -> Unit
) {
    var amountString by remember { mutableStateOf(initialBudget?.amount?.toPlainString() ?: "") }
    // -1 represents "Overall", null means no selection yet (for Add)
    var selectedCategoryId by remember { mutableStateOf(initialBudget?.categoryId ?: -1L) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categoriesIncludingOverall = remember(availableCategories) {
        listOf(Category(id = -1L, name = "Overall Budget")) + availableCategories
    }
    val selectedCategoryName = remember(selectedCategoryId, categoriesIncludingOverall) {
        categoriesIncludingOverall.find { it.id == selectedCategoryId }?.name ?: "Select Category"
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Category Selector (or "Overall")
                 ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                 ) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        label = { Text("Budget For") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categoriesIncludingOverall.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Budget Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text(NumberFormat.getCurrencyInstance().currency?.symbol ?: "$") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDecimal = amountString.toBigDecimalOrNull()
                    if (amountDecimal != null && amountDecimal > BigDecimal.ZERO) {
                        // Pass null for categoryId if "Overall" (-1) was selected
                        onConfirm(amountDecimal, selectedCategoryId.takeIf { it != -1L })
                    } else {
                        // TODO: Show validation error (e.g., using a Snackbar or state in dialog)
                    }
                },
                // Enable button only if amount is valid
                enabled = amountString.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}