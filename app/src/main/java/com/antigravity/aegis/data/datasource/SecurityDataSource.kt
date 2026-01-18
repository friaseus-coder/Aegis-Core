package com.antigravity.aegis.data.datasource

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_PIN_WRAPPED_MK_PREFIX = "pin_wrapped_mk_"
        private const val KEY_RECOVERY_WRAPPED_MK = "recovery_wrapped_mk"
        private const val KEY_IS_SETUP_DONE = "is_setup_done"
    }

    fun savePinWrappedMk(userId: Int, wrappedKey: ByteArray) {
        val encoded = Base64.encodeToString(wrappedKey, Base64.NO_WRAP)
        sharedPreferences.edit().putString(getPinnedWrappedKey(userId), encoded).apply()
    }

    fun getPinWrappedMk(userId: Int): ByteArray? {
        val encoded = sharedPreferences.getString(getPinnedWrappedKey(userId), null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    private fun getPinnedWrappedKey(userId: Int) = "${KEY_PIN_WRAPPED_MK_PREFIX}$userId"

    fun saveRecoveryWrappedMk(wrappedKey: ByteArray) {
        val encoded = Base64.encodeToString(wrappedKey, Base64.NO_WRAP)
        sharedPreferences.edit().putString(KEY_RECOVERY_WRAPPED_MK, encoded).apply()
    }

    fun getRecoveryWrappedMk(): ByteArray? {
        val encoded = sharedPreferences.getString(KEY_RECOVERY_WRAPPED_MK, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }
    
    fun setSetupDone(isDone: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SETUP_DONE, isDone).apply()
    }
    
    fun isSetupDone(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SETUP_DONE, false)
    }
}
