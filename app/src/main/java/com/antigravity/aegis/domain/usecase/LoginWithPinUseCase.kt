package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

class LoginWithPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(userId: Int, pin: String): Result<Unit> {
        return when (val result = authRepository.loginWithPin(userId, pin)) {
            is Result.Success -> {
                android.util.Log.d("EncryptionKeyManager", "Login successful. Setting Master Key of size: ${result.data.size}")
                encryptionKeyManager.setMasterKey(result.data)
                Result.Success(Unit)
            }
            is Result.Error -> {
                android.util.Log.e("EncryptionKeyManager", "Login failed", result.exception)
                Result.Error(result.exception)
            }
        }
    }
}

