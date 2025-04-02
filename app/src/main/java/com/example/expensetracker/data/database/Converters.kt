package com.example.expensetracker.data.database

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBigDecimalString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? {
        // Store BigDecimal as String to preserve precision
        return value?.toPlainString()
    }
}