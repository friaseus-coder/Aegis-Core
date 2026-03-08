package com.antigravity.aegis.data.repository

import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.domain.repository.SettingsRepository
import com.antigravity.aegis.domain.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AegisDatabase,
    private val backupRepository: com.antigravity.aegis.domain.repository.BackupRepository
) : SettingsRepository {

    override suspend fun exportDatabase(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbName = "aegis_core.db"
            val dbPath = context.getDatabasePath(dbName)
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            if (!dbPath.exists()) return@withContext Result.Error(Exception("Database file not found"))
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(dbPath).use { input -> input.copyTo(output) }
            } ?: return@withContext Result.Error(Exception("Could not open destination stream"))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun importDatabase(sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbName = "aegis_core.db"
            val dbPath = context.getDatabasePath(dbName)
            database.close()
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbPath).use { output -> input.copyTo(output) }
            } ?: return@withContext Result.Error(Exception("Could not open source stream"))
            val walFile = File(dbPath.parent, "$dbName-wal")
            val shmFile = File(dbPath.parent, "$dbName-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


    override fun getUserConfig(): kotlinx.coroutines.flow.Flow<com.antigravity.aegis.data.local.entity.UserConfig?> {
        return database.userConfigDao().getUserConfig()
    }

    override suspend fun updateLanguage(language: String) {
        // Ensure config exists first
        ensureConfigExists()
        database.userConfigDao().updateLanguage(language)
    }

    override suspend fun updateCurrency(currency: String) {
        ensureConfigExists()
        val current = database.userConfigDao().getUserConfigOneShot() ?: com.antigravity.aegis.data.local.entity.UserConfig()
        database.userConfigDao().insertOrUpdate(current.copy(id = 1, currency = currency))
    }

    override suspend fun updateThemeMode(mode: String) {
        ensureConfigExists()
        database.userConfigDao().updateThemeMode(mode)
    }

    override suspend fun insertOrUpdateConfig(config: com.antigravity.aegis.data.local.entity.UserConfig) {
        database.userConfigDao().insertOrUpdate(config)
    }


    private suspend fun ensureConfigExists() {
        val current = database.userConfigDao().getUserConfigOneShot()
        if (current == null) {
            database.userConfigDao().insertUserConfig(com.antigravity.aegis.data.local.entity.UserConfig())
        }
    }

    override suspend fun saveImageToInternalStorage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.Error(Exception("Cannot open input stream"))
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.Error(Exception("Cannot decode image"))
            inputStream.close()
            val maxDimension = 1024
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val scale = minOf(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height)
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                    if (it != originalBitmap) originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }
            val directory = File(context.filesDir, "company_assets")
            if (!directory.exists()) directory.mkdirs()
            val fileName = "company_logo_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)
            FileOutputStream(file).use { output ->
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
            }
            scaledBitmap.recycle()
            Result.Success(file.absolutePath)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun persistBackupUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            ensureConfigExists()
            val current = database.userConfigDao().getUserConfigOneShot()!!
            val newConfig = current.copy(backupLocationUri = uri.toString())
            database.userConfigDao().insertOrUpdate(newConfig)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Additional dependency: BackupRepository. 
    // Since SettingsRepositoryImpl is created via Hilt, we can request Provider<BackupRepository> if there's a circular dependency, 
    // or just inject BackupRepository if not. 
    // BackupRepository does NOT depend on SettingsRepository. So it's safe.
    // However, I need to add it to the constructor.
    // Let's assume I can modify the constructor.
    
    // As I cannot easily modify the constructor in this tool call without replacing the whole file header, 
    // I will use a different approach: directly calling the logic or creating a separate scoped use case. 
    // BUT the prompt said "Modify SettingsRepositoryImpl". 
    // The cleanest way is to use `BackupRepository` here.
    // I will assume BackupRepository is NOT available in this class yet. 
    // I will try to manually invoke the Backup logic or better: inject it.
    
    // Wait, to inject it I need to change the primary constructor at the top of the file.
    // This tool call is only replacing the bottom. 
    // I will do this in TWO steps. First add the method bodies (commented out or with TODO), then fix the constructor. 
    // ACTUALLY, I can replace the whole file or just use `find_by_name` to see if I can do a larger replacement.
    // No, I should use `replace_file_content` carefully. 
    
    // Alternative: Move `performAutoBackup` to a UseCase? 
    // The interface is in Repository. 
    
    // Let's assume for now I will duplicate the backup JSON creation logic OR 
    // (Better) I will change the constructor in the next step. 
    // For now I will put a placeholder or just use the DB directly as I have access to `database`.
    // `BackupRepository` logic is basically `database.dao...`.
    
    // Oh wait, `BackupRepository` uses Gson. I have access to Gson via injection? No.
    // I need Gson too.
    
    // Plan:
    // 1. Add methods `persistBackupUri`.
    // 2. `performAutoBackup` will perform the file writing. But obtaining the JSON string... 
    //    It calls `BackupRepository.createBackupJson()`.
    //    I really should inject BackupRepository.
    
    // I will skip implementing `performAutoBackup` fully here until I add BackupRepository to constructor.
    
    override suspend fun performAutoBackup(userConfig: com.antigravity.aegis.data.local.entity.UserConfig): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uriString = userConfig.backupLocationUri ?: return@withContext Result.Error(Exception("No backup location configured"))
            val treeUri = Uri.parse(uriString)

            val jsonString = when (val jsonResult = backupRepository.createBackupJson()) {
                is Result.Success -> jsonResult.data
                is Result.Error -> return@withContext Result.Error(jsonResult.exception)
            }

            val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
            if (docFile == null || !docFile.isDirectory) return@withContext Result.Error(Exception("Invalid directory URI"))

            val dateFormat = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            val timestamp = dateFormat.format(java.util.Date())
            val fileName = "aegis_backup_$timestamp.json"
            val newFile = docFile.createFile("application/json", fileName)
                ?: return@withContext Result.Error(Exception("Could not create file in directory"))

            context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                output.write(jsonString.toByteArray())
            } ?: return@withContext Result.Error(Exception("Could not open output stream"))

            val newConfig = userConfig.copy(lastBackupTimestamp = System.currentTimeMillis())
            database.userConfigDao().insertOrUpdate(newConfig)

            Result.Success(newFile.uri.toString())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createTemporaryBackupFile(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val jsonString = when (val jsonResult = backupRepository.createBackupJson()) {
                is Result.Success -> jsonResult.data
                is Result.Error -> return@withContext Result.Error(jsonResult.exception)
            }
            val dateFormat = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            val timestamp = dateFormat.format(java.util.Date())
            val fileName = "aegis_data_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { output -> output.write(jsonString.toByteArray()) }
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
