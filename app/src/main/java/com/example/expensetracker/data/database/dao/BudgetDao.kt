package com.example.expensetracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.database.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if budget for same category/month/year exists
    suspend fun insertOrUpdateBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget) // Likely same as insertOrUpdate due to OnConflictStrategy

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Long): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category_id = :categoryId")
    fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): Flow<Budget?>

    // Query for overall budget (assuming categoryId = -1 or null)
    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category_id IS NULL OR category_id = -1")
    fun getOverallBudgetForMonth(month: Int, year: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    fun getAllBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<Budget>>
}