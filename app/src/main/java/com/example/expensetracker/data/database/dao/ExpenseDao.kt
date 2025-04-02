package com.example.expensetracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.database.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long // Returns the new rowId

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Flow<Expense?> // Flow for observing changes

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>> // Flow for observing changes

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesBetweenDates(startDate: Date, endDate: Date): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category_id = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category_id = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByCategoryAndDate(categoryId: Long, startDate: Date, endDate: Date): Flow<List<Expense>>

    // Potentially add queries for aggregation (e.g., sum by category) later
    // @Query("SELECT category_id, SUM(amount) as total FROM expenses GROUP BY category_id")
    // fun getTotalAmountByCategory(): Flow<Map<Long, Double>> // Adjust return type as needed
}