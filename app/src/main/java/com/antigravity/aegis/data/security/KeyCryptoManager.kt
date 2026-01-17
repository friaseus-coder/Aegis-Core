package com.antigravity.aegis.data.security

import android.security.keystore.KeyProperties
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyCryptoManager @Inject constructor() {

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        
        // Iterations count - high for PIN (since PIN entropy is low) to slow down brute force
        private const val ITERATIONS = 15000 
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12 // GCM uses 12 byte IV generally
    }

    /**
     * Generates a random 32-byte (256-bit) master key.
     */
    fun generateMasterKey(): ByteArray {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * Wraps (Encrypts) the targetKey using a key derived from the password (PIN or Seed).
     * Returns: salt + iv + encryptedData
     */
    fun wrapKey(targetKey: ByteArray, password: CharArray): ByteArray {
        // 1. Generate Salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        // 2. Derive Key encryption Key (KEK) from password and salt
        val kek = deriveKey(password, salt)

        // 3. Encrypt targetKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, kek, GCMParameterSpec(128, iv))
        val encryptedBytes = cipher.doFinal(targetKey)

        // 4. Concat Salt + IV + Encrypted Data
        return salt + iv + encryptedBytes
    }

    /**
     * Unwraps (Decrypts) the wrappedBlob using the password.
     * Returns the original targetKey (Master Key).
     */
    fun unwrapKey(wrappedBlob: ByteArray, password: CharArray): ByteArray {
        // 1. Extract Salt, IV, Encrypted Data
        val salt = wrappedBlob.copyOfRange(0, SALT_LENGTH)
        val iv = wrappedBlob.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val encryptedBytes = wrappedBlob.copyOfRange(SALT_LENGTH + IV_LENGTH, wrappedBlob.size)

        // 2. Derive KEK
        val kek = deriveKey(password, salt)

        // 3. Decrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, kek, spec)
        
        return cipher.doFinal(encryptedBytes)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, AES_ALGORITHM)
    }
}
