package com.antigravity.aegis.domain.repository

interface AuthRepository {
    fun isSetupDone(): Boolean
    
    // Setup flow
    suspend fun createMasterKey(): ByteArray
    suspend fun saveCredentials(pin: String, seedPhrase: List<String>, masterKey: ByteArray): Result<Unit>
    
    // Login flow
    suspend fun loginWithPin(pin: String): Result<ByteArray> // Returns Master Key if success
    
    // Recovery flow
    suspend fun recoverWithSeed(seedPhrase: List<String>): Result<ByteArray> // Returns Master Key
}
