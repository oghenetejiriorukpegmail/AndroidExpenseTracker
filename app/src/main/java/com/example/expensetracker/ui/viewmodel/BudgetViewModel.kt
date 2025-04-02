package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.entity.Budget
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject

/**
 * Represents the data needed to display a budget item, including its progress.
 */
data class BudgetDisplayData(
    val budget: Budget,
    val categoryName: String?, // Null for overall budget
    val totalSpent: BigDecimal,
    val progress: Float // 0.0 to 1.0+
)

/**
 * UI state for the Budget screen.
 */
data class BudgetUiState(
    val budgetsWithProgress: List<BudgetDisplayData> = emptyList(),
    val availableCategories: List<Category> = emptyList(), // For adding/editing budgets
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val budgetToEdit: Budget? = null // For handling edit dialog state
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState(isLoading = true))
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    // TODO: Add state/logic for selecting month/year if needed

    init {
        loadBudgetData()
    }

    fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val month = _uiState.value.currentMonth
            val year = _uiState.value.currentYear

            // Combine flows for budgets, expenses (for the relevant month), and categories
            combine(
                expenseRepository.getAllBudgetsForMonth(month, year),
                expenseRepository.getAllExpenses(), // Fetch all for calculation simplicity, filter below
                expenseRepository.getAllCategories()
            ) { budgets, allExpenses, categories ->
                processBudgetData(budgets, allExpenses, categories, month, year)
            }.catch { exception ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error loading budget data: ${exception.message}")
                }
            }.collect { processedState ->
                _uiState.value = processedState
            }
        }
    }

    private fun processBudgetData(
        budgets: List<Budget>,
        allExpenses: List<Expense>,
        categories: List<Category>,
        month: Int,
        year: Int
    ): BudgetUiState {
        val categoryMap = categories.associateBy { it.id }

        // Filter expenses for the current month/year
        val expensesThisMonth = allExpenses.filter {
            val cal = Calendar.getInstance().apply { time = it.date }
            cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
        }

        val budgetsWithProgress = budgets.map { budget ->
            val totalSpent = if (budget.categoryId == null || budget.categoryId == -1L) {
                // Overall budget: sum all expenses for the month
                expensesThisMonth.sumOf { it.amount }
            } else {
                // Category-specific budget: sum expenses for that category
                expensesThisMonth.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
            }

            val progress = if (budget.amount > BigDecimal.ZERO) {
                (totalSpent.divide(budget.amount, 4, RoundingMode.HALF_UP)).toFloat().coerceAtMost(1.5f) // Cap progress display
            } else {
                0f // Avoid division by zero
            }

            BudgetDisplayData(
                budget = budget,
                categoryName = categoryMap[budget.categoryId]?.name,
                totalSpent = totalSpent,
                progress = progress
            )
        }

        return BudgetUiState(
            budgetsWithProgress = budgetsWithProgress,
            availableCategories = categories.filter { it.id != Category.UNCATEGORIZED.id }, // Exclude 'Uncategorized' from budget options
            currentMonth = month,
            currentYear = year,
            isLoading = false
        )
    }

    fun addOrUpdateBudget(amount: BigDecimal, categoryId: Long?) {
         if (amount <= BigDecimal.ZERO) {
             _uiState.update { it.copy(errorMessage = "Budget amount must be positive.") }
             return
         }
        val month = _uiState.value.currentMonth
        val year = _uiState.value.currentYear

        // Check if budget for this category/month already exists if needed, or rely on DAO REPLACE strategy
        val budget = Budget(
            // id will be auto-generated or replaced by DAO
            amount = amount,
            month = month,
            year = year,
            categoryId = categoryId // Null or -1 for overall
        )

        viewModelScope.launch {
            try {
                 _uiState.update { it.copy(errorMessage = null, budgetToEdit = null) }
                 expenseRepository.insertOrUpdateBudget(budget)
                 // Data reloads via flow collection
            } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Error saving budget: ${e.message}") }
            }
        }
    }

     fun deleteBudget(budget: Budget) {
         viewModelScope.launch {
             try {
                 _uiState.update { it.copy(errorMessage = null) }
                 expenseRepository.deleteBudget(budget)
             } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Error deleting budget: ${e.message}") }
             }
         }
     }

     // Functions to manage edit dialog state
     fun setBudgetToEdit(budget: Budget?) {
         _uiState.update { it.copy(budgetToEdit = budget, errorMessage = null) }
     }
}