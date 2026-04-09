package com.example.whoowesme.util

import android.content.Context
import android.net.Uri
import com.example.whoowesme.database.AppDatabase
import java.io.File

object DatabaseBackupManager {
    fun exportDatabase(context: Context, targetUri: Uri) {
        AppDatabase.getDatabase(context).openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(FULL)")
            .close()
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        require(dbFile.exists()) { "Database file not found" }

        context.contentResolver.openOutputStream(targetUri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Unable to open destination")
    }

    fun restoreDatabase(context: Context, sourceUri: Uri) {
        AppDatabase.closeDatabase()

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()

        deleteSidecarFiles(dbFile)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open backup file")

        deleteSidecarFiles(dbFile)
    }

    private fun deleteSidecarFiles(dbFile: File) {
        listOf(
            File("${dbFile.absolutePath}-wal"),
            File("${dbFile.absolutePath}-shm"),
            File("${dbFile.absolutePath}-journal")
        ).forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
