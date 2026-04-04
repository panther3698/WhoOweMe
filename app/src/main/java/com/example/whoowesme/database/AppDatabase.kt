package com.example.whoowesme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.whoowesme.database.dao.PersonDao
import com.example.whoowesme.database.dao.MoneyTransactionDao
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.MoneyTransaction

@Database(entities = [Person::class, MoneyTransaction::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): MoneyTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "who_owes_me_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
