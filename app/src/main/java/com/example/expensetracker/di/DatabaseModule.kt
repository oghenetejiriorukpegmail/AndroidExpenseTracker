package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.database.AppDatabase
import com.example.expensetracker.data.database.dao.BudgetDao
import com.example.expensetracker.data.database.dao.CategoryDao
import com.example.expensetracker.data.database.dao.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provides dependencies for the entire application lifecycle
object DatabaseModule {

    @Provides
    @Singleton // Ensure only one instance of the database is created
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        // Using Room.databaseBuilder directly here, Hilt manages the singleton instance
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "expense_tracker_database"
        )
        // Add callback for pre-population if needed (can also be done via AppDatabase companion object)
        // .addCallback(AppDatabase.roomCallback) // Example if using callback from AppDatabase
        .fallbackToDestructiveMigration() // Use proper migrations in production!
        .build()
    }

    @Provides
    // No need for @Singleton here, as AppDatabase is Singleton and provides DAOs
    fun provideExpenseDao(appDatabase: AppDatabase): ExpenseDao {
        return appDatabase.expenseDao()
    }

    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    fun provideBudgetDao(appDatabase: AppDatabase): BudgetDao {
        return appDatabase.budgetDao()
    }
}