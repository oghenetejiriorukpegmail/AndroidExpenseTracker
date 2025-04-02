package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

/**
 * Represents the UI state for the Add/Edit Expense screen.
 */
data class AddEditExpenseUiState(
    val amount: String = "", // Store as String for TextField input
    val date: Date = Date(), // Default to current date
    val selectedCategory: Category? = null,
    val availableCategories: List<Category> = emptyList(),
    val notes: String = "",
    val receiptImagePath: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false, // To know if we are editing an existing expense
    val expenseId: Long? = null // ID of the expense being edited
)

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditExpenseUiState())
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        // TODO: Add logic to load existing expense data if expenseId is passed for editing
    }

    private fun loadCategories() {
        viewModelScope.launch {
            expenseRepository.getAllCategories().collect { categories ->
                _uiState.update { currentState ->
                    currentState.copy(
                        availableCategories = categories,
                        // Set default category if not already set and categories are available
                        selectedCategory = currentState.selectedCategory ?: categories.firstOrNull { it.id == Category.UNCATEGORIZED.id } ?: categories.firstOrNull()
                    )
                }
            }
        }
    }

    fun updateAmount(newAmount: String) {
        // Basic validation can happen here or just update the string state
        _uiState.update { it.copy(amount = newAmount) }
    }

    fun updateDate(newDate: Date) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun updateCategory(newCategory: Category) {
        _uiState.update { it.copy(selectedCategory = newCategory) }
    }

    fun updateNotes(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun updateReceiptImagePath(path: String?) {
         _uiState.update { it.copy(receiptImagePath = path) }
    }

    fun saveExpense() {
        val currentState = _uiState.value
        val amountDecimal = currentState.amount.toBigDecimalOrNull()

        // --- Input Validation ---
        if (amountDecimal == null || amountDecimal <= BigDecimal.ZERO) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive amount.") }
            return
        }
        if (currentState.selectedCategory == null) {
             _uiState.update { it.copy(errorMessage = "Please select a category.") }
            return
        }
        // Clear previous error
        _uiState.update { it.copy(errorMessage = null, isSaving = true) }

        // --- Create or Update Expense ---
        val expenseToSave = Expense(
            id = currentState.expenseId ?: 0, // Use existing ID if editing, 0 for new
            amount = amountDecimal,
            date = currentState.date,
            categoryId = currentState.selectedCategory.id,
            notes = currentState.notes.takeIf { it.isNotBlank() }, // Save null if blank
            receiptImagePath = currentState.receiptImagePath
        )

        viewModelScope.launch {
            try {
                if (currentState.isEditMode) {
                    expenseRepository.updateExpense(expenseToSave)
                } else {
                    expenseRepository.insertExpense(expenseToSave)
                }
                // TODO: Add navigation back or success message handling
                 _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error saving expense: ${e.message}") }
            }
        }
    }

    // TODO: Add function loadExpenseForEditing(expenseId: Long)
}