package com.antigravity.aegis.domain.repository

import android.net.Uri

interface SettingsRepository {
    suspend fun exportDatabase(destinationUri: Uri): Result<Unit>
    suspend fun importDatabase(sourceUri: Uri): Result<Unit>
}
