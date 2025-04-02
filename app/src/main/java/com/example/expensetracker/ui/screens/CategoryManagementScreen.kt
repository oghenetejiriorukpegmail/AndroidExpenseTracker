package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.ui.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    // Use categoryToEdit from uiState to control edit dialog visibility
    val categoryToEdit = uiState.categoryToEdit

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else if (uiState.categories.isEmpty()) {
                     Text(
                        text = "No categories found. Add one using the '+' button.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryListItem(
                                category = category,
                                onEditClick = { viewModel.setCategoryToEdit(category) },
                                onDeleteClick = { viewModel.deleteCategory(category) },
                                // Disable editing/deleting for the default category
                                canModify = category.id != Category.UNCATEGORIZED.id
                            )
                            Divider()
                        }
                    }
                }

                // Add Category Dialog
                if (showAddDialog) {
                    CategoryEditDialog(
                        dialogTitle = "Add Category",
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name ->
                            viewModel.addCategory(name)
                            showAddDialog = false
                        }
                    )
                }

                // Edit Category Dialog
                categoryToEdit?.let { category ->
                     CategoryEditDialog(
                        dialogTitle = "Edit Category",
                        initialValue = category.name,
                        onDismiss = { viewModel.setCategoryToEdit(null) }, // Dismiss by clearing the state
                        onConfirm = { name ->
                            viewModel.updateCategory(category.copy(name = name))
                            // ViewModel handles clearing categoryToEdit on success/failure
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun CategoryListItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    canModify: Boolean,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(category.name) },
        modifier = modifier,
        trailingContent = {
            Row {
                IconButton(onClick = onEditClick, enabled = canModify) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Category")
                }
                IconButton(onClick = onDeleteClick, enabled = canModify) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = if(canModify) LocalContentColor.current else Color.Gray)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditDialog(
    dialogTitle: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(categoryName) },
                enabled = categoryName.isNotBlank() // Basic validation
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