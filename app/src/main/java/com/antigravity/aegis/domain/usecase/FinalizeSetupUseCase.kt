package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

class FinalizeSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(name: String, language: String, pin: String, role: com.antigravity.aegis.data.local.entity.UserRole, email: String?, phone: String?, seedPhrase: List<String>, masterKey: ByteArray): Result<Unit> {
        return when (val saveResult = authRepository.createAdmin(name, language, pin, role, email, phone, seedPhrase, masterKey)) {
            is Result.Success -> {
                encryptionKeyManager.setMasterKey(masterKey)
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(saveResult.exception)
        }
    }
}

