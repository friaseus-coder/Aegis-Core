package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.domain.util.Result

interface AuthRepository {
    fun isSetupDone(): Boolean
    fun hasOsCredentials(): Boolean
    suspend fun createMasterKey(): ByteArray
    suspend fun finalizeSetup(masterKey: ByteArray, cipher: javax.crypto.Cipher): Result<Unit>
    suspend fun updateOsKey(masterKey: ByteArray, cipher: javax.crypto.Cipher): Result<Unit>
    suspend fun loginWithOs(cipher: javax.crypto.Cipher): Result<ByteArray>
    fun getOsEncryptCipher(): javax.crypto.Cipher?
    fun getOsDecryptCipher(): javax.crypto.Cipher?
}
