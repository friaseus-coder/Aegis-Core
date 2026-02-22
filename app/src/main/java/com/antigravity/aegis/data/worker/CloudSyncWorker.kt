package com.antigravity.aegis.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.antigravity.aegis.data.cloud.GoogleDriveSyncManager
import com.antigravity.aegis.domain.repository.BackupRepository
import com.antigravity.aegis.domain.util.Result
import com.google.api.client.http.FileContent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val driveSyncManager: GoogleDriveSyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CloudSyncWorker", "Starting cloud synchronization...")

        val account = driveSyncManager.getLastSignedInAccount()
        if (account == null) {
            Log.w("CloudSyncWorker", "No Google account linked. Skipping sync.")
            return Result.success()
        }

        return try {
            val backupResult = backupRepository.createFullBackupZip()
            if (backupResult is com.antigravity.aegis.domain.util.Result.Success) {
                val backupFile = backupResult.data
                val driveService = driveSyncManager.getDriveService(account)

                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = backupFile.name
                    parents = listOf("appDataFolder")
                }
                
                val mediaContent = FileContent("application/octet-stream", backupFile)
                
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                Log.d("CloudSyncWorker", "Backup uploaded successfully to Google Drive.")
                
                // Keep only the latest local backup file or cleanup?
                // For now, let's keep it but ideally we should manage local space.
                
                Result.success()
            } else {
                Log.e("CloudSyncWorker", "Failed to create full backup zip.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("CloudSyncWorker", "Error during cloud sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "CloudSyncWork"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<CloudSyncWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
