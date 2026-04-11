package com.example.whoowesme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whoowesme.database.dao.PersonDao
import com.example.whoowesme.database.dao.MoneyTransactionDao
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.MoneyTransaction

@Database(entities = [Person::class, MoneyTransaction::class], version = 6, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): MoneyTransactionDao

    companion object {
        const val DATABASE_NAME = "who_owes_me_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATIONS = arrayOf(
            object : Migration(1, 6) { override fun migrate(db: SupportSQLiteDatabase) = ensureV6Schema(db) },
            object : Migration(2, 6) { override fun migrate(db: SupportSQLiteDatabase) = ensureV6Schema(db) },
            object : Migration(3, 6) { override fun migrate(db: SupportSQLiteDatabase) = ensureV6Schema(db) },
            object : Migration(4, 6) { override fun migrate(db: SupportSQLiteDatabase) = ensureV6Schema(db) },
            object : Migration(5, 6) { override fun migrate(db: SupportSQLiteDatabase) = ensureV6Schema(db) }
        )

        private fun ensureV6Schema(db: SupportSQLiteDatabase) {
            // People table
            addColumnIfNotExists(db, "people", "phoneNumber", "TEXT NOT NULL DEFAULT ''")
            addColumnIfNotExists(db, "people", "notes", "TEXT NOT NULL DEFAULT ''")
            addColumnIfNotExists(db, "people", "createdAt", "INTEGER NOT NULL DEFAULT 0")

            // Transactions table
            addColumnIfNotExists(db, "transactions", "dueDate", "INTEGER")
            addColumnIfNotExists(db, "transactions", "promisedPaymentDate", "INTEGER")
            addColumnIfNotExists(db, "transactions", "recurrenceFrequency", "TEXT NOT NULL DEFAULT 'NONE'")
            addColumnIfNotExists(db, "transactions", "recurringSeriesId", "TEXT")
            addColumnIfNotExists(db, "transactions", "note", "TEXT NOT NULL DEFAULT ''")
            addColumnIfNotExists(db, "transactions", "createdAt", "INTEGER NOT NULL DEFAULT 0")
        }

        private fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnType: String) {
            val cursor = db.query("PRAGMA table_info(`$tableName`)")
            var exists = false
            try {
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex) == columnName) {
                            exists = true
                            break
                        }
                    }
                }
            } finally {
                cursor.close()
            }
            if (!exists) {
                db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnType")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(*MIGRATIONS)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
