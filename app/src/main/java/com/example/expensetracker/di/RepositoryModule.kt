package com.example.expensetracker.di

import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.ExpenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provides dependencies for the entire application lifecycle
abstract class RepositoryModule {

    @Binds
    @Singleton // Ensure the bound implementation is also treated as a singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository // Binds ExpenseRepository interface to ExpenseRepositoryImpl implementation
}