package com.example.whoowesme.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whoowesme.R
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.util.MoneyFormatter

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val personId = inputData.getLong("personId", -1)
        val amount = inputData.getDouble("amount", 0.0)
        val typeOrdinal = inputData.getInt("type", -1)
        val personName = inputData.getString("personName") ?: "Someone"

        if (personId == -1L || typeOrdinal == -1) return Result.failure()

        val type = TransactionType.entries[typeOrdinal]
        val message = if (type == TransactionType.GIVEN) {
            "Reminder: $personName owes you ${MoneyFormatter.format(amount)}"
        } else {
            "Reminder: You owe $personName ${MoneyFormatter.format(amount)}"
        }

        sendNotification(message)

        return Result.success()
    }

    private fun sendNotification(message: String) {
        val channelId = "reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transaction Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Who Owes Me")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Fallback icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
