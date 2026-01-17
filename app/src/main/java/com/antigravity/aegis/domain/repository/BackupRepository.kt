package com.antigravity.aegis.domain.repository

import android.net.Uri

interface BackupRepository {
    suspend fun exportBackup(uri: Uri, password: String): Result<Unit>
    suspend fun importBackup(uri: Uri, password: String): Result<Unit>
    suspend fun createAutoBackup(tag: String): Result<String> // Returns path
}
