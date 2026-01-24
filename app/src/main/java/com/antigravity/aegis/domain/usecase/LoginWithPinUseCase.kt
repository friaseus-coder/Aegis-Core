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
            android.util.Log.d("EncryptionKeyManager", "Login successful. Setting Master Key of size: ${masterKey.size}")
            // Store raw bytes directly, no need for Base64 encoding for memory storage
            encryptionKeyManager.setMasterKey(masterKey)
            Result.success(Unit)
        } else {
            val error = result.exceptionOrNull()
            android.util.Log.e("EncryptionKeyManager", "Login failed", error)
            Result.failure(error ?: Exception("Unknown error"))
        }
    }
}
