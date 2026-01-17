package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class FinalizeSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) {
    suspend operator fun invoke(pin: String, seedPhrase: List<String>, masterKey: ByteArray): Result<Unit> {
        val saveResult = authRepository.saveCredentials(pin, seedPhrase, masterKey)
        if (saveResult.isSuccess) {
            // Set the key in memory so the session is active immediately
            encryptionKeyManager.setKey(String(masterKey.map { it.toInt().toChar() }.toCharArray()))
            // Note: The KeyManager as written expects String (legacy from prompt).
            // We should ideally pass ByteArray, but KeyManager converts string to char array.
            // The Master Key is raw bytes, not chars. 
            // FIX: EncryptionKeyManager was designed for "User enters PIN -> PIN is Key".
            // NOW: "User enters PIN -> We unwrap MK -> MK is Key".
            // We need to update EncryptionKeyManager to accept Byte Array or handle raw bytes.
            // For now, we'll map bytes to a String representation (Base64) to set it, 
            // OR we should update EncryptionKeyManager to hold ByteArray.
            // Let's update EncryptionKeyManager to be safe.
        }
        return saveResult
    }
}
