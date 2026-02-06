package com.antigravity.aegis.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.CrmRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class BudgetFollowUpWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val crmRepository: CrmRepository // Using CrmRepository as it has getQuotesByStatus
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Check for quotes sent > 14 days ago
            val sentQuotes = crmRepository.getQuotesByStatus("Sent").first()
            val fourteenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)

            sentQuotes.forEach { quote ->
                // Check if last log is old enough or if quote date is old enough
                // Assuming quote property 'date' is creation/sent date if status is Sent
                // Or check logs. For simplicity, checking quote.date
                if (quote.date < fourteenDaysAgo) {
                    // Trigger Notification
                    sendNotification(quote.id, quote.title)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun sendNotification(quoteId: Int, quoteTitle: String) {
        val channelId = "budget_followup_benefit"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Budget Follow Up", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            // .setSmallIcon(R.drawable.ic_notification) // Ensure this resource exists or use android default
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Seguimiento de Presupuesto")
            .setContentText("El presupuesto '$quoteTitle' (#$quoteId) lleva más de 14 días enviado. ¿Usar plantilla de cierre?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(quoteId, notification)
    }
}
