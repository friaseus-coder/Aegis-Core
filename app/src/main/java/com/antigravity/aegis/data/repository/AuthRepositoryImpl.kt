package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.datasource.SecurityDataSource
import com.antigravity.aegis.data.security.KeyCryptoManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val securityDataSource: SecurityDataSource,
    private val keyCryptoManager: KeyCryptoManager
) : AuthRepository {

    override fun isSetupDone(): Boolean {
        return securityDataSource.isSetupDone()
    }

    override suspend fun createMasterKey(): ByteArray {
        return keyCryptoManager.generateMasterKey()
    }

    override suspend fun saveCredentials(
        pin: String,
        seedPhrase: List<String>,
        masterKey: ByteArray
    ): Result<Unit> {
        return try {
            // 1. Wrap MK with PIN
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, pin.toCharArray())
            
            // 2. Wrap MK with Seed
            val seedString = seedPhrase.joinToString(" ") // Space separated
            val recoveryWrapped = keyCryptoManager.wrapKey(masterKey, seedString.toCharArray())
            
            // 3. Save
            securityDataSource.savePinWrappedMk(pinWrapped)
            securityDataSource.saveRecoveryWrappedMk(recoveryWrapped)
            securityDataSource.setSetupDone(true)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithPin(pin: String): Result<ByteArray> {
        return try {
            val wrapped = securityDataSource.getPinWrappedMk()
                ?: return Result.failure(Exception("No PIN credentials found"))
            
            val mk = keyCryptoManager.unwrapKey(wrapped, pin.toCharArray())
            Result.success(mk)
        } catch (e: Exception) {
            // Decryption failed = Wrong PIN
            Result.failure(e)
        }
    }

    override suspend fun recoverWithSeed(seedPhrase: List<String>): Result<ByteArray> {
        return try {
            val wrapped = securityDataSource.getRecoveryWrappedMk()
                ?: return Result.failure(Exception("No recovery credentials found"))
            
            val seedString = seedPhrase.joinToString(" ")
            val mk = keyCryptoManager.unwrapKey(wrapped, seedString.toCharArray())
            Result.success(mk)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
