package com.example.whoowesme.repository

import com.example.whoowesme.database.dao.PersonDao
import com.example.whoowesme.database.dao.MoneyTransactionDao
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val personDao: PersonDao,
    private val transactionDao: MoneyTransactionDao
) {
    val allPeople: Flow<List<Person>> = personDao.getAllPeople()
    val allTransactions: Flow<List<MoneyTransaction>> = transactionDao.getAllTransactions()

    fun getTransactionsForPerson(personId: Long): Flow<List<MoneyTransaction>> =
        transactionDao.getTransactionsForPerson(personId)

    suspend fun getTransactionById(transactionId: Long): MoneyTransaction? =
        transactionDao.getTransactionById(transactionId)

    suspend fun getPersonById(personId: Long): Person? = personDao.getPersonById(personId)

    suspend fun insertPerson(person: Person): Long = personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)

    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    suspend fun insertTransaction(transaction: MoneyTransaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: MoneyTransaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: MoneyTransaction) = transactionDao.deleteTransaction(transaction)
}
