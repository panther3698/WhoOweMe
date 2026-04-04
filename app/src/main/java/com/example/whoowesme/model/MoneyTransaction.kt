package com.example.whoowesme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.whoowesme.model.enums.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["personId"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val personId: Long,
    val type: TransactionType,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
