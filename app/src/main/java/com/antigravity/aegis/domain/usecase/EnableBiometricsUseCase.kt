package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.crypto.Cipher
import javax.inject.Inject

class EnableBiometricsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: Int, masterKey: ByteArray, cipher: Cipher): Result<Unit> {
        return authRepository.enableBiometric(userId, masterKey, cipher)
    }
}

