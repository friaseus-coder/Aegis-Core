package com.antigravity.aegis.data.datasource

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("aegis_secure_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_OS_WRAPPED_MK = "os_wrapped_mk"
        private const val KEY_IS_SETUP_DONE = "is_setup_done"
        private const val KEY_VAULT_DISCLAIMER_ACCEPTED = "vault_disclaimer_accepted"
    }

    fun saveOsWrappedMk(wrappedKey: ByteArray) {
        val encoded = Base64.encodeToString(wrappedKey, Base64.NO_WRAP)
        sharedPreferences.edit().putString(KEY_OS_WRAPPED_MK, encoded).apply()
    }

    fun getOsWrappedMk(): ByteArray? {
        val encoded = sharedPreferences.getString(KEY_OS_WRAPPED_MK, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }



    fun setSetupDone(isDone: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SETUP_DONE, isDone).apply()
    }
    
    fun isSetupDone(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SETUP_DONE, false)
    }

    fun setVaultDisclaimerAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_VAULT_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    fun isVaultDisclaimerAccepted(): Boolean {
        return sharedPreferences.getBoolean(KEY_VAULT_DISCLAIMER_ACCEPTED, false)
    }
}
