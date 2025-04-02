package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.dao.BudgetDao
import com.example.expensetracker.data.database.dao.CategoryDao
import com.example.expensetracker.data.database.dao.ExpenseDao
import com.example.expensetracker.data.database.entity.Budget
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of ExpenseRepository using Room DAOs.
 * Marked as Singleton so Hilt provides the same instance throughout the app.
 */
@Singleton // Ensures a single instance of the repository
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : ExpenseRepository {

    // --- Expense Operations ---
    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    override fun getExpenseById(id: Long): Flow<Expense?> = expenseDao.getExpenseById(id)
    override fun getExpensesBetweenDates(startDate: Date, endDate: Date): Flow<List<Expense>> =
        expenseDao.getExpensesBetweenDates(startDate, endDate)
    override fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(categoryId)
    override fun getExpensesByCategoryAndDate(categoryId: Long, startDate: Date, endDate: Date): Flow<List<Expense>> =
        expenseDao.getExpensesByCategoryAndDate(categoryId, startDate, endDate)
    override suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    override suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    override suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // --- Category Operations ---
    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override fun getCategoryById(id: Long): Flow<Category?> = categoryDao.getCategoryById(id)
    override suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)
    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    override suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    override suspend fun insertDefaultCategoriesIfNeeded() {
        // Check if 'Uncategorized' exists, insert if not.
        // This might be better handled by the database creation callback, but can be here too.
        if (categoryDao.getCategoryByName(Category.UNCATEGORIZED.name) == null) {
            categoryDao.insertCategory(Category.UNCATEGORIZED)
        }
        // Add other default categories if desired (e.g., "Food", "Transport")
        // Example:
        // if (categoryDao.getCategoryByName("Food") == null) {
        //     categoryDao.insertCategory(Category(name = "Food"))
        // }
    }

    // --- Budget Operations ---
    override fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()
    override fun getBudgetById(id: Long): Flow<Budget?> = budgetDao.getBudgetById(id)
    override fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): Flow<Budget?> =
        budgetDao.getBudgetForCategoryAndMonth(categoryId, month, year)
    override fun getOverallBudgetForMonth(month: Int, year: Int): Flow<Budget?> =
        budgetDao.getOverallBudgetForMonth(month, year)
    override fun getAllBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getAllBudgetsForMonth(month, year)
    override suspend fun insertOrUpdateBudget(budget: Budget): Long = budgetDao.insertOrUpdateBudget(budget)
    override suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)
}