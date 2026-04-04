package com.example.whoowesme.database.dao

import androidx.room.*
import com.example.whoowesme.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyTransactionDao {
    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC")
    fun getTransactionsForPerson(personId: Long): Flow<List<MoneyTransaction>>

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun getTransactionById(transactionId: Long): MoneyTransaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<MoneyTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MoneyTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: MoneyTransaction): Int

    @Delete
    suspend fun deleteTransaction(transaction: MoneyTransaction): Int
}
