package com.example.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.database.dao.BudgetDao
import com.example.expensetracker.data.database.dao.CategoryDao
import com.example.expensetracker.data.database.dao.ExpenseDao
import com.example.expensetracker.data.database.entity.Budget
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.database.entity.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Expense::class, Category::class, Budget::class],
    version = 1, // Increment version on schema changes
    exportSchema = false // Schema export is recommended for production apps but false for simplicity here
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Callback to pre-populate the database (e.g., add default category)
        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default data on creation
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Add default 'Uncategorized' category
                        database.categoryDao().insertCategory(Category.UNCATEGORIZED)
                        // Add other default data if needed
                    }
                }
            }
        }

        // Hilt will handle providing the instance, but this is traditional singleton pattern
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database" // Database file name
                )
                .addCallback(roomCallback) // Add the callback here
                // .fallbackToDestructiveMigration() // Use migrations in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}