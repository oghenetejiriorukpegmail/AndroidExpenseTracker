package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.entity.Budget
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for accessing expense, category, and budget data.
 * Abstracts the data source (e.g., Room database) from the ViewModels.
 */
interface ExpenseRepository {

    // Expense Operations
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpenseById(id: Long): Flow<Expense?>
    fun getExpensesBetweenDates(startDate: Date, endDate: Date): Flow<List<Expense>>
    fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>>
    fun getExpensesByCategoryAndDate(categoryId: Long, startDate: Date, endDate: Date): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)

    // Category Operations
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: Long): Flow<Category?>
    suspend fun getCategoryByName(name: String): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun insertDefaultCategoriesIfNeeded() // Helper to ensure defaults exist

    // Budget Operations
    fun getAllBudgets(): Flow<List<Budget>>
    fun getBudgetById(id: Long): Flow<Budget?>
    fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): Flow<Budget?>
    fun getOverallBudgetForMonth(month: Int, year: Int): Flow<Budget?>
    fun getAllBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    suspend fun insertOrUpdateBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)

}