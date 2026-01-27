package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class FinalizeSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(name: String, language: String, pin: String, role: com.antigravity.aegis.data.model.UserRole, email: String?, phone: String?, seedPhrase: List<String>, masterKey: ByteArray): Result<Unit> {
        val saveResult = authRepository.createAdmin(name, language, pin, role, email, phone, seedPhrase, masterKey)
        if (saveResult.isSuccess) {
            // Set the key in memory so the session is active immediately
            // Use raw bytes directly for consistency with LoginWithPinUseCase
            encryptionKeyManager.setMasterKey(masterKey)
            return Result.success(Unit)
        }
        return Result.failure(saveResult.exceptionOrNull() ?: Exception("Unknown error"))
    }
}
