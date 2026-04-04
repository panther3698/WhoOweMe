package com.example.whoowesme.database

import androidx.room.TypeConverter
import com.example.whoowesme.model.enums.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
