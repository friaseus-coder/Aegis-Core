package com.antigravity.aegis.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.antigravity.aegis.R
import java.util.concurrent.TimeUnit

class QuoteFollowUpWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val quoteId = inputData.getInt(KEY_QUOTE_ID, -1)
        val title = inputData.getString(KEY_TITLE) ?: "Presupuesto"
        val timesRemaining = inputData.getInt(KEY_TIMES_REMAINING, 0)
        val daysInterval = inputData.getInt(KEY_DAYS_INTERVAL, 7)

        if (quoteId == -1 || timesRemaining <= 0) {
            return Result.success() // No valid data or no more reminders needed
        }

        showNotification(quoteId, title)

        // Reschedule next occurrence if needed
        val nextTimesRemaining = timesRemaining - 1
        if (nextTimesRemaining > 0) {
            scheduleNextReminder(appContext, quoteId, title, daysInterval, nextTimesRemaining)
        }

        return Result.success()
    }

    private fun showNotification(quoteId: Int, title: String) {
        val channelId = "quote_reminders_channel"
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Presupuesto",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para seguimiento de presupuestos enviados"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Ideally add PendingIntent to open the app, keeping simple for now
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Seguimiento: $title")
            .setContentText("Recuerda hacer seguimiento al proyecto en estado 'Enviado'.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(quoteId, notification)
    }

    companion object {
        const val KEY_QUOTE_ID = "quote_id"
        const val KEY_TITLE = "title"
        const val KEY_TIMES_REMAINING = "times_remaining"
        const val KEY_DAYS_INTERVAL = "days_interval"

        fun scheduleNextReminder(context: Context, quoteId: Int, title: String, daysInterval: Int, timesRemaining: Int) {
            val data = Data.Builder()
                .putInt(KEY_QUOTE_ID, quoteId)
                .putString(KEY_TITLE, title)
                .putInt(KEY_TIMES_REMAINING, timesRemaining)
                .putInt(KEY_DAYS_INTERVAL, daysInterval)
                .build()

            val request = OneTimeWorkRequestBuilder<QuoteFollowUpWorker>()
                .setInitialDelay(daysInterval.toLong(), TimeUnit.DAYS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "quote_reminder_$quoteId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
