package com.antigravity.aegis.domain.repository

import android.net.Uri

import com.antigravity.aegis.data.model.UserConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun exportDatabase(destinationUri: Uri): Result<Unit>
    suspend fun importDatabase(sourceUri: Uri): Result<Unit>
    
    fun getUserConfig(): Flow<UserConfig?>
    suspend fun updateLanguage(language: String)
    suspend fun updateThemeMode(mode: String)
    suspend fun insertOrUpdateConfig(config: UserConfig)
}
