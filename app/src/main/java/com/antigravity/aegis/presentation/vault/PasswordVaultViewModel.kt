package com.antigravity.aegis.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.dao.PasswordDao
import com.antigravity.aegis.data.local.entity.PasswordEntity
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.data.security.KeyCryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordVaultViewModel @Inject constructor(
    private val passwordDao: PasswordDao,
    private val encryptionKeyManager: EncryptionKeyManager,
    private val cryptoManager: KeyCryptoManager
) : ViewModel() {

    private val _allPasswords = passwordDao.getAllPasswords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPasswords = _allPasswords

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    private val _decryptedPasswords = MutableStateFlow<Map<Int, String>>(emptyMap())
    val decryptedPasswords = _decryptedPasswords.asStateFlow()

    fun unlockVault() {
        // Biometric auth would be triggered here in the UI, 
        // but the ViewModel just needs to know if it's authorized to "unlock" locally 
        // using the key already in EncryptionKeyManager.
        if (encryptionKeyManager.isKeySet()) {
            _isUnlocked.value = true
        }
    }

    fun lockVault() {
        _isUnlocked.value = false
        _decryptedPasswords.value = emptyMap()
    }

    /**
     * Decrypts a specific password "on-the-fly".
     */
    fun decryptPassword(passwordEntity: PasswordEntity) {
        if (!_isUnlocked.value) return
        
        val masterKey = encryptionKeyManager.getKey() ?: return
        
        viewModelScope.launch {
            try {
                val decrypted = cryptoManager.decryptData(passwordEntity.encryptedPassword, masterKey)
                _decryptedPasswords.update { it + (passwordEntity.id to decrypted) }
            } catch (e: Exception) {
                // Handle decryption error
            }
        }
    }

    fun addPassword(title: String, username: String, plainText: String, website: String?, notes: String?) {
        val masterKey = encryptionKeyManager.getKey() ?: return
        
        viewModelScope.launch {
            val encrypted = cryptoManager.encryptData(plainText, masterKey)
            val entity = PasswordEntity(
                title = title,
                username = username,
                encryptedPassword = encrypted,
                website = website,
                notes = notes
            )
            passwordDao.insertPassword(entity)
        }
    }

    fun deletePassword(password: PasswordEntity) {
        viewModelScope.launch {
            passwordDao.deletePassword(password)
        }
    }
}
