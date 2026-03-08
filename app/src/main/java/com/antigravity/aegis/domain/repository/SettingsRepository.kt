package com.antigravity.aegis.domain.repository

import android.net.Uri

import com.antigravity.aegis.data.local.entity.UserConfig
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun exportDatabase(destinationUri: Uri): Result<Unit>
    suspend fun importDatabase(sourceUri: Uri): Result<Unit>

    fun getUserConfig(): Flow<UserConfig?>
    suspend fun updateLanguage(language: String)
    suspend fun updateCurrency(currency: String)
    suspend fun updateThemeMode(mode: String)
    suspend fun insertOrUpdateConfig(config: UserConfig)

    suspend fun saveImageToInternalStorage(uri: Uri): Result<String>

    suspend fun persistBackupUri(uri: Uri): Result<Unit>
    suspend fun performAutoBackup(userConfig: UserConfig): Result<String>
    suspend fun createTemporaryBackupFile(): Result<java.io.File>
}

