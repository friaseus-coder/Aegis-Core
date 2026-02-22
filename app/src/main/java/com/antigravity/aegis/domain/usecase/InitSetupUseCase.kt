package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

data class SetupState(
    val masterKey: ByteArray
)

class InitSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): SetupState {
        val masterKey = authRepository.createMasterKey()
        return SetupState(masterKey)
    }
}
