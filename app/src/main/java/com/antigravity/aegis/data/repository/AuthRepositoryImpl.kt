package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.datasource.SecurityDataSource
import com.antigravity.aegis.data.security.KeyCryptoManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val securityDataSource: SecurityDataSource,
    private val keyCryptoManager: KeyCryptoManager,
    private val biometricCryptoManager: com.antigravity.aegis.data.security.BiometricCryptoManager
) : AuthRepository {

    override fun isSetupDone(): Boolean {
        return securityDataSource.isSetupDone()
    }

    override fun hasOsCredentials(): Boolean {
        return securityDataSource.getOsWrappedMk() != null
    }

    override suspend fun finalizeSetup(
        masterKey: ByteArray,
        cipher: javax.crypto.Cipher
    ): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        try {
            // 1. Wrap MK with OS Cipher
            val encryptedMk = biometricCryptoManager.encryptData(masterKey, cipher)
            val iv = cipher.iv
            val combined = iv + encryptedMk
            securityDataSource.saveOsWrappedMk(combined)

            // 2. Set Setup Done
            securityDataSource.setSetupDone(true)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateOsKey(
        masterKey: ByteArray,
        cipher: javax.crypto.Cipher
    ): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        try {
            val encryptedMk = biometricCryptoManager.encryptData(masterKey, cipher)
            val iv = cipher.iv
            val combined = iv + encryptedMk
            securityDataSource.saveOsWrappedMk(combined)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createMasterKey(): ByteArray {
        return keyCryptoManager.generateMasterKey()
    }

    override suspend fun loginWithOs(cipher: javax.crypto.Cipher): Result<ByteArray> {
        return try {
            val combined = securityDataSource.getOsWrappedMk()
                ?: return Result.Error(Exception("No OS credentials found"))

            // Extract IV (12 bytes) and Data
            val iv = combined.copyOfRange(0, 12)
            val encryptedData = combined.copyOfRange(12, combined.size)

            val mk = biometricCryptoManager.decryptData(encryptedData, cipher)
            Result.Success(mk)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }



    override fun getOsEncryptCipher(): javax.crypto.Cipher? {
        return biometricCryptoManager.getEncryptCipher("os_lock_key")
    }

    override fun getOsDecryptCipher(): javax.crypto.Cipher? {
        val combined = securityDataSource.getOsWrappedMk() ?: return null
        val iv = combined.copyOfRange(0, 12)
        return biometricCryptoManager.getDecryptCipher("os_lock_key", iv)
    }
}

