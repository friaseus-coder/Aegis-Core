package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class FinalizeSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(name: String, language: String, pin: String, seedPhrase: List<String>, masterKey: ByteArray): Result<Unit> {
        val saveResult = authRepository.createAdmin(name, language, pin, seedPhrase, masterKey)
        if (saveResult.isSuccess) {
            // Set the key in memory so the session is active immediately
            // Using Base64 to store byte array as string key for now
             val encodedKey = android.util.Base64.encodeToString(masterKey, android.util.Base64.NO_WRAP)
            encryptionKeyManager.setKey(encodedKey)
            return Result.success(Unit)
        }
        return Result.failure(saveResult.exceptionOrNull() ?: Exception("Unknown error"))
    }
}
