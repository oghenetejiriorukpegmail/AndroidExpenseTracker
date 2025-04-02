package com.example.expensetracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date // Using java.util.Date for simplicity, consider alternatives like kotlinx-datetime

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Category::class, // Define relationship with Category entity (to be created)
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.SET_DEFAULT // Or CASCADE, RESTRICT depending on desired behavior
    )],
    indices = [Index(value = ["category_id"])] // Index for faster queries based on category
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: BigDecimal, // Use BigDecimal for precise currency handling

    @ColumnInfo(name = "date")
    val date: Date, // Store date

    @ColumnInfo(name = "category_id", defaultValue = "1") // Default to an 'Uncategorized' category ID
    val categoryId: Long,

    @ColumnInfo(name = "notes")
    val notes: String?, // Optional notes

    @ColumnInfo(name = "receipt_image_path")
    val receiptImagePath: String? // Optional path to the stored receipt image
)