package com.antigravity.aegis.data.security

import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionKeyManager @Inject constructor() {

    private var passphrase: ByteArray? = null

    fun setKey(pin: String) {
        android.util.Log.d("EncryptionKeyManager", "setKey called with PIN length: ${pin.length}")
        clearKey()
        passphrase = pin.toByteArray(Charsets.UTF_8)
        android.util.Log.d("EncryptionKeyManager", "Key set from PIN. Is set? ${isKeySet()}")
    }

    fun setMasterKey(key: ByteArray) {
        android.util.Log.d("EncryptionKeyManager", "setMasterKey called with bytes length: ${key.size}")
        clearKey()
        passphrase = key.clone()
        android.util.Log.d("EncryptionKeyManager", "Master Key set. Is set? ${isKeySet()}")
    }

    fun getKey(): ByteArray? {
        val key = passphrase
        android.util.Log.d("EncryptionKeyManager", "getKey called. Returning null? ${key == null}")
        return key
    }

    fun isKeySet(): Boolean {
        return passphrase != null && passphrase!!.isNotEmpty()
    }

    fun clearKey() {
        android.util.Log.d("EncryptionKeyManager", "clearKey called")
        passphrase?.let {
            Arrays.fill(it, 0.toByte())
        }
        passphrase = null
    }
}
