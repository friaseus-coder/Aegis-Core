package com.antigravity.aegis.data.repository

import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AegisDatabase // Injected to close it or checkpoint it
) : SettingsRepository {

    override suspend fun exportDatabase(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Checkpoint to ensure WAL is merged to DB file
            // Note: If using WAL, we should export -shm and -wal too, OR checkpoint.
            // Checkpoint is safer for single file export.
            val dbName = "aegis_core.db"
            val dbPath = context.getDatabasePath(dbName)
            
            // Force checkpoint (This assumes we can access the SupportSQLiteDatabase)
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            
            if (!dbPath.exists()) return@withContext Result.failure(Exception("Database file not found"))

            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(dbPath).use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open destination stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importDatabase(sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbName = "aegis_core.db"
            val dbPath = context.getDatabasePath(dbName)
            
            // Close DB connections if possible? Room doesn't expose easy close-reopen without destroying instance.
            // Dangerous operation while App is running. Ideally we should kill process after import.
            // Or use Room's close().
            database.close()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open source stream"))
            
            // Delete WAL and SHM to avoid corruption on restart
            val walFile = File(dbPath.parent, "$dbName-wal")
            val shmFile = File(dbPath.parent, "$dbName-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
