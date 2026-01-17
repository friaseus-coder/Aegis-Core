package com.antigravity.aegis.data.security

import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionKeyManager @Inject constructor() {

    private var passphrase: ByteArray? = null

    fun setKey(pin: String) {
        clearKey()
        // If we are using PIN as key (Legacy mode or dev), we convert to bytes.
        // But in our new architecture, we shouldn't use PIN directly as DB Key.
        // However, for compatibility or intermediate steps, we might need it.
        passphrase = pin.toByteArray(Charsets.UTF_8)
    }

    fun setMasterKey(key: ByteArray) {
        clearKey()
        passphrase = key.clone() // Store a copy
    }

    /**
     * Returns the current key.
     * WARNING: This returns the reference to the sensitive array.
     */
    fun getKey(): ByteArray? {
        return passphrase
    }

    fun isKeySet(): Boolean {
        return passphrase != null && passphrase!!.isNotEmpty()
    }

    fun clearKey() {
        passphrase?.let {
            Arrays.fill(it, 0.toByte()) // Zero out the memory
        }
        passphrase = null
    }
}
