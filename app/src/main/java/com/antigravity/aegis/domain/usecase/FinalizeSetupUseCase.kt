package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

class FinalizeSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(
        masterKey: ByteArray,
        cipher: javax.crypto.Cipher
    ): Result<Unit> {
        return when (val saveResult = authRepository.finalizeSetup(masterKey, cipher)) {
            is Result.Success -> {
                encryptionKeyManager.setMasterKey(masterKey)
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(saveResult.exception)
        }
    }
}

