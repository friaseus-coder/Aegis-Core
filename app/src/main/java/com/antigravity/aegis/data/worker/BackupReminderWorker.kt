package com.antigravity.aegis.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.antigravity.aegis.MainActivity
import com.antigravity.aegis.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BackupReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BackupReminderWorker", "Ejecutando recordatorio semanal de backup en la nube")
        
        showNotification()
        
        return Result.success()
    }

    private fun showNotification() {
        // En Android 13+ comprobamos que tenemos el permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("BackupReminderWorker", "No se tienen permisos de notificación (Android 13+)")
                return
            }
        }

        createNotificationChannel()

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Reemplazar con el icono adecuado si existe uno tipo ic_stat_backup
            .setContentTitle("Copia de Seguridad")
            .setContentText("¡Hora de guardar tus datos! Toca aquí para subir tu copia a la Nube.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(appContext)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("BackupReminderWorker", "Error de seguridad al enviar notificación", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Seguridad"
            val descriptionText = "Canal para recordar a los usuarios que renueven sus copias de seguridad"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val WORK_NAME = "BackupReminderWork"
        private const val CHANNEL_ID = "BACKUP_REMINDERS_CHANNEL"
        private const val NOTIFICATION_ID = 1001

        fun enqueue(context: Context) {
            // Un recordatorio no necesita internet necesariamente para dispararse, pero lo ideal es que lo tenga para Drive
            val constraints = Constraints.Builder()
                .build()

            // Recordatorio configurado para saltar cada 7 días
            val syncRequest = PeriodicWorkRequestBuilder<BackupReminderWorker>(
                7, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
