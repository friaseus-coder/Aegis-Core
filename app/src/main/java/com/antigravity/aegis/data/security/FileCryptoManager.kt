package com.antigravity.aegis.data.security

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileCryptoManager @Inject constructor() {

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        
        private const val ITERATIONS = 15000 
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12
    }

    /**
     * Encrypts the data and writes it to the OutputStream.
     * Format: SALT (16 bytes) + IV (12 bytes) + ENCRYPTED_DATA
     */
    fun encrypt(outputStream: OutputStream, data: ByteArray, password: String) {
        try {
            // 1. Generate Salt
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)

            // 2. Generate IV
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // 3. Derive Key
            val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, AES_ALGORITHM)

            // 4. Init Cipher
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            // 5. Encrypt
            val encryptedData = cipher.doFinal(data)

            // 6. Write to stream
            outputStream.write(salt)
            outputStream.write(iv)
            outputStream.write(encryptedData)
        } finally {
            outputStream.close()
        }
    }

    /**
     * Reads from InputStream, decrypts and returns the raw data.
     * Expects Format: SALT (16 bytes) + IV (12 bytes) + ENCRYPTED_DATA
     */
    fun decrypt(inputStream: InputStream, password: String): ByteArray {
        return inputStream.use { input ->
            // 1. Read Salt
            val salt = ByteArray(SALT_LENGTH)
            if (input.read(salt) != SALT_LENGTH) throw IllegalArgumentException("Invalid file format")

            // 2. Read IV
            val iv = ByteArray(IV_LENGTH)
            if (input.read(iv) != IV_LENGTH) throw IllegalArgumentException("Invalid file format")

            // 3. Read Encrypted Data
            val encryptedData = input.readBytes()

            // 4. Derive Key
            val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, AES_ALGORITHM)

            // 5. Decrypt
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            cipher.doFinal(encryptedData)
        }
    }
}
