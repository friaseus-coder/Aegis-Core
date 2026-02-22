package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.*
import com.antigravity.aegis.domain.util.Result
import java.io.File

interface BackupRepository {
    suspend fun createBackupJson(): Result<String>
    suspend fun restoreBackupJson(json: String): Result<Unit>
    suspend fun createFullBackupZip(): Result<File>
}

