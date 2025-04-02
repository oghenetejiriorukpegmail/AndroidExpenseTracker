package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * UI state for the History screen.
 */
data class HistoryUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(), // For filtering and display
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filterStartDate: Date? = null,
    val filterEndDate: Date? = null,
    val filterCategoryId: Long? = null
)

@OptIn(ExperimentalCoroutinesApi::class) // For flatMapLatest
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterCriteria())
    val filterState: StateFlow<FilterCriteria> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories() // Load categories once for the filter dropdown

        // Combine categories flow and filtered expenses flow into the final UI state
        viewModelScope.launch {
            combine(
                expenseRepository.getAllCategories(), // Flow for categories
                _filterState.flatMapLatest { criteria -> // React to filter changes
                    fetchFilteredExpenses(criteria)
                }
            ) { categories, filteredExpensesResult ->
                // filteredExpensesResult is a Pair<List<Expense>, String?>
                HistoryUiState(
                    expenses = filteredExpensesResult.first,
                    categories = categories,
                    isLoading = false, // Loading is handled within fetchFilteredExpenses
                    errorMessage = filteredExpensesResult.second, // Error from fetching expenses
                    filterStartDate = _filterState.value.startDate,
                    filterEndDate = _filterState.value.endDate,
                    filterCategoryId = _filterState.value.categoryId
                )
            }.catch { exception ->
                // Catch errors from combine or initial flows
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error loading history: ${exception.message}")
                }
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    private fun loadCategories() {
        // Categories are loaded via the combine operator now
    }

    // Fetches expenses based on current filter criteria
    private fun fetchFilteredExpenses(criteria: FilterCriteria): Flow<Pair<List<Expense>, String?>> {
        // Determine which repository function to call based on filters
        val expenseFlow: Flow<List<Expense>> = when {
            criteria.categoryId != null && criteria.startDate != null && criteria.endDate != null ->
                expenseRepository.getExpensesByCategoryAndDate(criteria.categoryId, criteria.startDate, criteria.endDate)
            criteria.categoryId != null ->
                expenseRepository.getExpensesByCategory(criteria.categoryId)
            criteria.startDate != null && criteria.endDate != null ->
                expenseRepository.getExpensesBetweenDates(criteria.startDate, criteria.endDate)
            else ->
                expenseRepository.getAllExpenses()
        }

        // Map the flow to include loading state and potential errors
        return expenseFlow
            .map<List<Expense>, Pair<List<Expense>, String?>> { expenses -> Pair(expenses, null) } // Success case
            .onStart { emit(Pair(emptyList(), null)) } // Optional: Clear list on new fetch start
            .catch { exception ->
                emit(Pair(emptyList(), "Error fetching expenses: ${exception.message}")) // Error case
            }
    }

    // --- Filter Update Functions ---
    fun updateStartDate(date: Date?) {
        _filterState.update { it.copy(startDate = date) }
    }

    fun updateEndDate(date: Date?) {
        _filterState.update { it.copy(endDate = date) }
    }

    fun updateCategoryFilter(categoryId: Long?) {
        _filterState.update { it.copy(categoryId = categoryId) }
    }

    fun clearFilters() {
        _filterState.value = FilterCriteria() // Reset to default
    }

    // Helper data class for filter state
    data class FilterCriteria(
        val startDate: Date? = null,
        val endDate: Date? = null,
        val categoryId: Long? = null
    )
}