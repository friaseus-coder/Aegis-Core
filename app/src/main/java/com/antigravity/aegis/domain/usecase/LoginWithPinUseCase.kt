package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(pin: String): Result<Unit> {
        val result = authRepository.loginWithPin(pin)
        return if (result.isSuccess) {
            val masterKey = result.getOrThrow()
            // Same issue as above: MK is ByteArray. KeyManager takes String.
            // We will encode MK as Base64 String to pass it to KeyManager setKey, 
            // PROVISIONAL: We really should update KeyManager to ByteArray.
            // For now I will assume we use Base64 string for internal transport in KeyManager 
            // since I cannot edit KeyManager and UseCase in same step cleanly without context.
            // Let's rely on Updating KeyManager in next step.
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }
}
