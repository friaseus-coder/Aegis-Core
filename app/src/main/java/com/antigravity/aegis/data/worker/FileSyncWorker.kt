package com.antigravity.aegis.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.antigravity.aegis.data.cloud.GoogleDriveSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Trabajador encargado de realizar backups periódicos a Google Drive.
 */
@HiltWorker
class FileSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val driveSyncManager: GoogleDriveSyncManager,
    private val notificationHelper: com.antigravity.aegis.data.util.SyncNotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val fileId = driveSyncManager.uploadDatabaseBackup()
            if (fileId != null) {
                notificationHelper.showSyncSuccessNotification()
                Result.success()
            } else {
                // Puede ser que no haya iniciado sesión o falle la red
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
