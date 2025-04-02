package com.example.expensetracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Represents a budget limit. Can be overall or category-specific for a given period (e.g., month/year).
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.CASCADE // If a category is deleted, associated budgets are also deleted
    )],
    // Index to ensure only one budget per category per period (or one overall budget per period)
    // Using month and year columns for simplicity. A Date might be more flexible.
    indices = [
        Index(value = ["category_id", "month", "year"], unique = true),
        Index(value = ["category_id"]) // Index for faster lookups by category
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: BigDecimal, // The budget limit amount

    @ColumnInfo(name = "month") // e.g., 1 for January, 12 for December
    val month: Int,

    @ColumnInfo(name = "year") // e.g., 2025
    val year: Int,

    @ColumnInfo(name = "category_id", defaultValue = "-1") // -1 could represent an overall budget, or link to a Category ID
    val categoryId: Long? // Nullable or use a sentinel value like -1 for overall budget
)