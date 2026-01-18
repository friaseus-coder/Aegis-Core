package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(userId: Int, pin: String): Result<Unit> {
        val result = authRepository.loginWithPin(userId, pin)
        return if (result.isSuccess) {
            val masterKey = result.getOrThrow()
            val encodedKey = android.util.Base64.encodeToString(masterKey, android.util.Base64.NO_WRAP)
            encryptionKeyManager.setKey(encodedKey)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }
}
