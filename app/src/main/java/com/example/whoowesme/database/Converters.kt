package com.example.whoowesme.database

import androidx.room.TypeConverter
import com.example.whoowesme.model.enums.RecurrenceFrequency
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

    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
        return RecurrenceFrequency.valueOf(value)
    }
}
