package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.core.model.pie.PieChartModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject

/**
 * UI state for the Overview screen.
 */
data class OverviewUiState(
    val totalExpensesThisMonth: BigDecimal = BigDecimal.ZERO,
    val categorySpending: Map<String, Float> = emptyMap(), // Category Name -> Amount (as Float for Vico)
    val monthlySpending: Map<Int, Float> = emptyMap(), // Month (1-12) -> Amount (as Float for Vico)
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OverviewUiState(isLoading = true))
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    // Vico Model Producers (optional, can also build models directly in composable)
    val categoryPieChartProducer: PieChartModelProducer = PieChartModelProducer.build()
    val monthlyBarChartProducer: CartesianChartModelProducer = CartesianChartModelProducer.build()


    init {
        loadOverviewData()
    }

    fun loadOverviewData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Combine flows for all expenses and all categories
            combine(
                expenseRepository.getAllExpenses(),
                expenseRepository.getAllCategories()
            ) { expenses, categories ->
                processChartData(expenses, categories)
            }.catch { exception ->
                // Handle errors from combine or underlying flows
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error loading overview data: ${exception.message}")
                }
            }.collect { processedState ->
                _uiState.value = processedState
                // Optional: Update Vico producers if using them
                // updateChartProducers(processedState)
            }
        }
    }

    private fun processChartData(expenses: List<Expense>, categories: List<Category>): OverviewUiState {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Calculate total for the current month
        val totalThisMonth = expenses.filter {
            val cal = Calendar.getInstance().apply { time = it.date }
            cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        // Calculate spending per category for the current month
        val categoryMap = categories.associateBy { it.id }
        val spendingByCategory = expenses
            .filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }
            .groupBy { it.categoryId }
            .mapKeys { categoryMap[it.key]?.name ?: "Unknown" }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() } // Convert to Float for Vico
            .filter { it.value > 0 } // Only include categories with spending

        // Calculate spending per month for the current year (simplified)
        val spendingByMonth = expenses
             .filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.get(Calendar.YEAR) == currentYear
            }
            .groupBy { Calendar.getInstance().apply { time = it.date }.get(Calendar.MONTH) + 1 } // Group by month (1-12)
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .toSortedMap() // Ensure months are ordered

        return OverviewUiState(
            totalExpensesThisMonth = totalThisMonth,
            categorySpending = spendingByCategory,
            monthlySpending = spendingByMonth,
            isLoading = false
        )
    }

    // Optional: Function to update Vico producers if managing them here
    private fun updateChartProducers(state: OverviewUiState) {
        // Example for Pie Chart (needs PieChartModel.Entry)
        // val pieEntries = state.categorySpending.map { PieChartModel.Entry(it.key, it.value) }
        // categoryPieChartProducer.tryRunTransaction { /* update model here */ }

        // Example for Bar Chart (needs CartesianChartModel)
        // monthlyBarChartProducer.tryRunTransaction { /* update model here */ }
    }

}