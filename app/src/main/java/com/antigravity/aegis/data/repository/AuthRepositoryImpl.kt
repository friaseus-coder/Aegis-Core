package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.datasource.SecurityDataSource
import com.antigravity.aegis.data.security.KeyCryptoManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.local.entity.UserEntity
import com.antigravity.aegis.data.local.entity.UserRole
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl @Inject constructor(
    private val securityDataSource: SecurityDataSource,
    private val keyCryptoManager: KeyCryptoManager,
    private val biometricCryptoManager: com.antigravity.aegis.data.security.BiometricCryptoManager,
    private val userEntityDao: UserEntityDao
) : AuthRepository {

    override fun isSetupDone(): Boolean {
        return securityDataSource.isSetupDone()
    }
    
    override fun getAllUsers(): Flow<List<UserEntity>> {
        return userEntityDao.getAllUsers()
    }

    override suspend fun getUserById(userId: Int): UserEntity? {
        return userEntityDao.getUserById(userId)
    }

    override suspend fun createMasterKey(): ByteArray {
        return keyCryptoManager.generateMasterKey()
    }

    override suspend fun createAdmin(
        name: String,
        language: String,
        pin: String,
        role: UserRole,
        email: String?,
        phone: String?,
        seedPhrase: List<String>,
        masterKey: ByteArray
    ): Result<UserEntity> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        try {
            // 1. Create User Entity
            val user = UserEntity(name = name, language = language, role = role, email = email, phone = phone)
            val userId = userEntityDao.insertOrUpdate(user).toInt()
            val savedUser = user.copy(id = userId)

            // 2. Wrap MK with PIN and Save per User
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, pin.toCharArray())
            securityDataSource.savePinWrappedMk(userId, pinWrapped)
            
            // 3. Wrap MK with Seed (Global/Recovery) - Using 2 words now
            val seedString = seedPhrase.joinToString(" ") // Space separated
            val recoveryWrapped = keyCryptoManager.wrapKey(masterKey, seedString.toCharArray())
            securityDataSource.saveRecoveryWrappedMk(recoveryWrapped)
            
            // 3b. Wrap MK with Email+Phone if provided
            if (!email.isNullOrBlank() && !phone.isNullOrBlank()) {
                 val combined = "$email|$phone"
                 val emailPhoneWrapped = keyCryptoManager.wrapKey(masterKey, combined.toCharArray())
                 securityDataSource.saveEmailPhoneWrappedMk(userId, emailPhoneWrapped)
            }
            
            // 4. Set Setup Done
            securityDataSource.setSetupDone(true)
            
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUser(
        name: String, 
        language: String,
        pin: String, 
        role: UserRole, 
        email: String?,
        phone: String?,
        masterKey: ByteArray
    ): Result<UserEntity> {
        return try {
             // 1. Create User
            val user = UserEntity(name = name, language = language, role = role, email = email, phone = phone)
            val userId = userEntityDao.insertOrUpdate(user).toInt()
            val savedUser = user.copy(id = userId)
            
            // 2. Wrap MK with new PIN
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, pin.toCharArray())
            securityDataSource.savePinWrappedMk(userId, pinWrapped)
            
            // 3. Wrap MK with Email+Phone if provided
            if (!email.isNullOrBlank() && !phone.isNullOrBlank()) {
                 val combined = "$email|$phone"
                 val emailPhoneWrapped = keyCryptoManager.wrapKey(masterKey, combined.toCharArray())
                 securityDataSource.saveEmailPhoneWrappedMk(userId, emailPhoneWrapped)
            }
            
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(userId: Int, role: UserRole): Result<Unit> {
        return try {
            userEntityDao.updateUserRole(userId, role)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePin(userId: Int, newPin: String, masterKey: ByteArray): Result<Unit> {
        return try {
            // Re-wrap MK with new PIN
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, newPin.toCharArray())
            securityDataSource.savePinWrappedMk(userId, pinWrapped)
            
            // Note: We should also verify if forcePinChange was true and set it to false?
            // UserEntity currently has forcePinChange. We should update the user entity too.
             val user = userEntityDao.getUserById(userId)
             if (user != null && user.forcePinChange) {
                 userEntityDao.insertOrUpdate(user.copy(forcePinChange = false))
             }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithPin(userId: Int, pin: String): Result<ByteArray> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        try {
            val wrapped = securityDataSource.getPinWrappedMk(userId)
                ?: return@withContext Result.failure(Exception("No PIN credentials found for user $userId"))
            
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

    override suspend fun recoverWithEmailPhone(userId: Int, email: String, phone: String): Result<ByteArray> {
        return try {
            // Reconstruct the key string
            val combined = "$email|$phone"
            
            val wrapped = securityDataSource.getEmailPhoneWrappedMk(userId)
                ?: return Result.failure(Exception("No recovery credentials found for email/phone"))
                
            val mk = keyCryptoManager.unwrapKey(wrapped, combined.toCharArray())
            Result.success(mk)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enableBiometric(
        userId: Int,
        masterKey: ByteArray,
        cipher: javax.crypto.Cipher
    ): Result<Unit> {
        return try {
            val encryptedMk = biometricCryptoManager.encryptData(masterKey, cipher)
            // We append IV to encrypted data for storage if needed, 
            // but BiometricCryptoManager might handle it.
            // Let's check: BiometricCryptoManager.encryptData just calls doFinal.
            // AES/GCM needs IV. The IV is in cipher.iv
            
            val iv = cipher.iv
            val combined = iv + encryptedMk
            
            securityDataSource.saveBiometricWrappedMk(userId, combined)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithBiometric(
        userId: Int,
        cipher: javax.crypto.Cipher
    ): Result<ByteArray> {
         return try {
             val combined = securityDataSource.getBiometricWrappedMk(userId)
                 ?: return Result.failure(Exception("No biometric credentials found"))
             
             // Extract IV (12 bytes) and Data
             val iv = combined.copyOfRange(0, 12)
             val encryptedData = combined.copyOfRange(12, combined.size)
             
             val mk = biometricCryptoManager.decryptData(encryptedData, cipher)
             Result.success(mk)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    override fun getBiometricEncryptCipher(userId: Int): javax.crypto.Cipher? {
        return biometricCryptoManager.getEncryptCipher("biometric_key_$userId")
    }

    override fun getBiometricDecryptCipher(userId: Int): javax.crypto.Cipher? {
        val combined = securityDataSource.getBiometricWrappedMk(userId) ?: return null
        val iv = combined.copyOfRange(0, 12)
        return biometricCryptoManager.getDecryptCipher("biometric_key_$userId", iv)
    }

    override fun isBiometricEnabled(userId: Int): Boolean {
        return securityDataSource.getBiometricWrappedMk(userId) != null
    }
}
