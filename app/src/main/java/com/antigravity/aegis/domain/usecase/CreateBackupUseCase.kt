package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.BackupRepository
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<String> {
        return backupRepository.createBackupJson()
    }
}
