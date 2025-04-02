package com.example.expensetracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)] // Ensure category names are unique
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String
) {
    companion object {
        // Define a default 'Uncategorized' category often needed
        val UNCATEGORIZED = Category(id = 1, name = "Uncategorized")
    }
}