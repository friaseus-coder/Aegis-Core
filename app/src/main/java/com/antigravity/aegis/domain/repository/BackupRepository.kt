package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.model.*
import kotlinx.coroutines.flow.Flow

interface BackupRepository {
    suspend fun createBackupJson(): Result<String>
    suspend fun restoreBackupJson(json: String): Result<Unit>
}
