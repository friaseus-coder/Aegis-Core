package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.datasource.SecurityDataSource
import com.antigravity.aegis.data.security.KeyCryptoManager
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.model.UserEntity
import com.antigravity.aegis.data.model.UserRole
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl @Inject constructor(
    private val securityDataSource: SecurityDataSource,
    private val keyCryptoManager: KeyCryptoManager,
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
        seedPhrase: List<String>,
        masterKey: ByteArray
    ): Result<UserEntity> {
        return try {
            // 1. Create User Entity
            val user = UserEntity(name = name, language = language, role = UserRole.ADMIN)
            val userId = userEntityDao.insertOrUpdate(user).toInt()
            val savedUser = user.copy(id = userId)

            // 2. Wrap MK with PIN and Save per User
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, pin.toCharArray())
            securityDataSource.savePinWrappedMk(userId, pinWrapped)
            
            // 3. Wrap MK with Seed (Global/Recovery)
            val seedString = seedPhrase.joinToString(" ") // Space separated
            val recoveryWrapped = keyCryptoManager.wrapKey(masterKey, seedString.toCharArray())
            securityDataSource.saveRecoveryWrappedMk(recoveryWrapped)
            
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
        masterKey: ByteArray
    ): Result<UserEntity> {
        return try {
             // 1. Create User
            val user = UserEntity(name = name, language = language, role = role)
            val userId = userEntityDao.insertOrUpdate(user).toInt()
            val savedUser = user.copy(id = userId)
            
            // 2. Wrap MK with new PIN
            val pinWrapped = keyCryptoManager.wrapKey(masterKey, pin.toCharArray())
            securityDataSource.savePinWrappedMk(userId, pinWrapped)
            
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithPin(userId: Int, pin: String): Result<ByteArray> {
        return try {
            val wrapped = securityDataSource.getPinWrappedMk(userId)
                ?: return Result.failure(Exception("No PIN credentials found for user $userId"))
            
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
