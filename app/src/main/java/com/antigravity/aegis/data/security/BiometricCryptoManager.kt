package com.antigravity.aegis.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricCryptoManager @Inject constructor() {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    private val keyGenerator: KeyGenerator by lazy {
        KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
    }

    fun getEncryptCipher(keyName: String): Cipher? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateKey(keyName) ?: return null
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            cipher
        } catch (e: Exception) {
            android.util.Log.e("BiometricCryptoManager", "Error getting encrypt cipher", e)
            null
        }
    }

    fun getDecryptCipher(keyName: String, iv: ByteArray): Cipher? {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getKey(keyName) ?: return null
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher
    }

    fun encryptData(data: ByteArray, cipher: Cipher): ByteArray {
        return cipher.doFinal(data)
    }

    fun decryptData(encryptedData: ByteArray, cipher: Cipher): ByteArray {
        return cipher.doFinal(encryptedData)
    }

    private fun getOrCreateKey(keyName: String): SecretKey? {
        return if (keyStore.containsAlias(keyName)) {
            getKey(keyName)
        } else {
            generateSecretKey(keyName)
        }
    }

    private fun getKey(keyName: String): SecretKey? {
        return keyStore.getKey(keyName, null) as? SecretKey
    }

    private fun generateSecretKey(keyName: String): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                15, // 15 seconds validity
                KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(15)
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
    
    companion object {
        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
    }
}
