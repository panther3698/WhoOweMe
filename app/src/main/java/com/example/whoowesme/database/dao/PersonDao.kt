package com.example.whoowesme.database.dao

import androidx.room.*
import com.example.whoowesme.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE personId = :personId")
    suspend fun getPersonById(personId: Long): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person): Int

    @Delete
    suspend fun deletePerson(person: Person): Int
}
