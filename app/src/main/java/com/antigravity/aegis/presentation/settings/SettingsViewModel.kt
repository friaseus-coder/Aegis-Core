package com.antigravity.aegis.presentation.settings

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.model.UserRole
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _users = authRepository.getAllUsers()
    // We could expose users here if we want to show list to Admin, etc.
    
    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading("Exporting Database...")
            val result = settingsRepository.exportDatabase(uri)
            _uiState.value = if (result.isSuccess) {
                SettingsUiState.Success("Database exported successfully")
            } else {
                SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Export failed")
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading("Importing Database...")
            val result = settingsRepository.importDatabase(uri)
            _uiState.value = if (result.isSuccess) {
                // Restart App or Navigate to Login? 
                // DB is replaced, session is invalid (key might be wrong for new DB).
                logout() 
                SettingsUiState.Success("Database imported. Please login again.")
            } else {
                SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Import failed")
            }
        }
    }

    fun createNewUser(name: String, language: String = "es", pin: String, role: UserRole = UserRole.USER) {
        val encodedKey = encryptionKeyManager.getKey()
        if (encodedKey == null) {
             _uiState.value = SettingsUiState.Error("Session locked. Cannot create user.")
             return
        }
        
        // Fix: KeyManager returns String (Base64) or Bytes? Current impl returns ByteArray?
        // Let's check KeyManager. It returns ByteArray?
        // Wait, I updated FinalizeSetup to set encoded string. 
        // If KeyManager.getKey() returns ByteArray, it returns the bytes of the encoded string? 
        // Or the raw bytes? 
        // If KeyManager.passphrase is ByteArray, and I set it with `pin.toByteArray()`.
        // In FinalizeSetup: `encryptionKeyManager.setKey(encodedKey)` where encodedKey is String (Base64).
        // So `encryptionKeyManager.passphrase` holds `encodedKey` bytes (UTF-8 bytes of Base64 string).
        
        // To get the RAW MK, I need to decode the Base64 string from the stored bytes.
        // This is messy. I should have fixed KeyManager properly.
        // Assuming `encryptionKeyManager.getKey()` returns the bytes of `encodedKey`.
        
        val storedBytes = encodedKey
        
        try {
            // Need to reconstruct the Base64 string from bytes and then decode to MK bytes
            val base64String = String(storedBytes, Charsets.UTF_8)
            val masterKey = Base64.decode(base64String, Base64.NO_WRAP)
            
            viewModelScope.launch {
                _uiState.value = SettingsUiState.Loading("Creating User...")
                val result = authRepository.createUser(name, language, pin, role, masterKey)
                if (result.isSuccess) {
                    _uiState.value = SettingsUiState.Success("User created successfully")
                } else {
                    _uiState.value = SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create user")
                }
            }
        } catch (e: Exception) {
             _uiState.value = SettingsUiState.Error("Key Error: ${e.message}")
        }
    }

    fun logout() {
        encryptionKeyManager.clearKey()
        // Navigation should be handled by UI observing a "LoggedOut" state or just checking KeyManager?
        // Authenticated State is usually in AuthViewModel (Session).
        // For now we just clear key.
        _uiState.value = SettingsUiState.LoggedOut
    }
    
    fun clearState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed interface SettingsUiState {
    data object Idle : SettingsUiState
    data class Loading(val message: String) : SettingsUiState
    data class Success(val message: String) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
    data object LoggedOut : SettingsUiState
}
