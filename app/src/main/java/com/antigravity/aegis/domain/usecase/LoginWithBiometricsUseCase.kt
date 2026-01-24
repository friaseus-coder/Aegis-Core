package com.antigravity.aegis.domain.usecase

import android.util.Base64
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.crypto.Cipher
import javax.inject.Inject

class LoginWithBiometricsUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(userId: Int, cipher: Cipher): Result<Unit> {
        val result = authRepository.loginWithBiometric(userId, cipher)
        return if (result.isSuccess) {
            val masterKey = result.getOrThrow()
            // We need to encode it to String to set it in EncryptionKeyManager (which expects String PIN or Encoded Key)
            // Wait, EncryptionKeyManager has setMasterKey(byteArray)
            encryptionKeyManager.setMasterKey(masterKey)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Biometric login failed"))
        }
    }
}
