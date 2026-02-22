package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.crypto.Cipher
import javax.inject.Inject

class LoginWithBiometricsUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(userId: Int, cipher: Cipher): Result<Unit> {
        return when (val result = authRepository.loginWithBiometric(userId, cipher)) {
            is Result.Success -> {
                encryptionKeyManager.setMasterKey(result.data)
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(result.exception)
        }
    }
}

