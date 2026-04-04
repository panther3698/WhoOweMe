package com.example.whoowesme.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whoowesme.database.AppDatabase
import com.example.whoowesme.repository.AppRepository
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
// import kotlinx.coroutines.tasks.await

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        // val db = AppDatabase.getDatabase(applicationContext)
        // val repository = AppRepository(db.personDao(), db.transactionDao())
        // val firestore = FirebaseFirestore.getInstance()

        return try {
            /*
            // 1. Sync People
            val localPeople = repository.allPeople.first()
            localPeople.forEach { person ->
                firestore.collection("users").document(userId)
                    .collection("people").document(person.personId.toString())
                    .set(person)
                    .await()
            }

            // 2. Sync Transactions
            val localTransactions = repository.allTransactions.first()
            localTransactions.forEach { transaction ->
                firestore.collection("users").document(userId)
                    .collection("transactions").document(transaction.transactionId.toString())
                    .set(transaction)
                    .await()
            }
            */
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
