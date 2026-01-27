package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(json: String): Result<Unit> {
        return backupRepository.restoreBackupJson(json)
    }
}
