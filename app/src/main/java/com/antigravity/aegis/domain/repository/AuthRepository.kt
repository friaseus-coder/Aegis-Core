package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.model.UserEntity
import com.antigravity.aegis.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isSetupDone(): Boolean
    
    // User Management
    fun getAllUsers(): Flow<List<UserEntity>>
    suspend fun getUserById(userId: Int): UserEntity?
    
    // Setup flow (First Admin)
    suspend fun createMasterKey(): ByteArray
    suspend fun createAdmin(name: String, pin: String, seedPhrase: List<String>, masterKey: ByteArray): Result<UserEntity>
    
    // Add subsequent users (requires current decrypted Master Key)
    suspend fun createUser(name: String, pin: String, role: UserRole, masterKey: ByteArray): Result<UserEntity>

    // Login flow
    suspend fun loginWithPin(userId: Int, pin: String): Result<ByteArray> // Returns Master Key if success
    
    // Recovery flow (Resets everything or recovers Admin?) - For now, let's keep simple recovery that returns MK
    suspend fun recoverWithSeed(seedPhrase: List<String>): Result<ByteArray> // Returns Master Key
}
