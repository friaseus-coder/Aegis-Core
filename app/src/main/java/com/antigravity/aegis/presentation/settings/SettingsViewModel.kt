package com.antigravity.aegis.presentation.settings

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.model.UserRole
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.repository.SettingsRepository
import com.antigravity.aegis.domain.usecase.EnableBiometricsUseCase
import com.antigravity.aegis.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val encryptionKeyManager: EncryptionKeyManager,
    private val enableBiometricsUseCase: EnableBiometricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _biometricPromptState = MutableStateFlow<BiometricPromptConfig?>(null)
    val biometricPromptState = _biometricPromptState.asStateFlow()
    
    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled = _isBiometricEnabled.asStateFlow()
    
    // User Config
    val userConfig = settingsRepository.getUserConfig()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(language)
            _uiState.value = SettingsUiState.Success("Idioma cambiado a ${if(language == "es") "Español" else "English"}")
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
            _uiState.value = SettingsUiState.Success("Tema cambiado")
        }
    }
    
    // Current user ID - loaded from users
    private var currentUserId: Int = 1
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            authRepository.getAllUsers().collect { users ->
                if (users.isNotEmpty()) {
                    currentUserId = users.first().id
                    _isBiometricEnabled.value = authRepository.isBiometricEnabled(currentUserId)
                }
            }
        }
    }

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
        val masterKey = encryptionKeyManager.getKey()
        if (masterKey == null) {
             _uiState.value = SettingsUiState.Error("Session locked. Cannot create user.")
             return
        }
        
        // getKey() now returns raw bytes directly (not Base64 encoded)
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading("Creating User...")
            val result = authRepository.createUser(name, language, pin, role, masterKey)
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.Success("User created successfully")
            } else {
                _uiState.value = SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create user")
            }
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
    
    private var pendingBiometricAction: ((javax.crypto.Cipher) -> Unit)? = null
    
    fun enableBiometric() {
        _uiState.value = SettingsUiState.Loading("Iniciando configuración biométrica...")
        
        android.util.Log.d("EncryptionKeyManager", "SettingsViewModel: enableBiometric called. Checking key...")
        if (encryptionKeyManager.isKeySet()) {
             android.util.Log.d("EncryptionKeyManager", "SettingsViewModel: Key IS set.")
        } else {
             android.util.Log.d("EncryptionKeyManager", "SettingsViewModel: Key IS NOT set.")
        }
        
        val mk = encryptionKeyManager.getKey() ?: run {
            android.util.Log.e("EncryptionKeyManager", "SettingsViewModel: getKey() returned null. Session blocked. Forcing logout.")
            // Instead of just showing Error, we logout the user because session key is lost (Process Death)
            _uiState.value = SettingsUiState.LoggedOut
            return
        }
        
        viewModelScope.launch {
            val cipher = authRepository.getBiometricEncryptCipher(currentUserId)
            if (cipher != null) {
                pendingBiometricAction = { validCipher ->
                    viewModelScope.launch {
                        val result = enableBiometricsUseCase(currentUserId, mk, validCipher)
                        if (result.isSuccess) {
                            _isBiometricEnabled.value = true
                            _uiState.value = SettingsUiState.Success("Biometría activada correctamente")
                        } else {
                            _uiState.value = SettingsUiState.Error("Error al activar biometría")
                        }
                    }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.biometric_enable_title,
                    descriptionResId = R.string.biometric_enable_desc,
                    cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            } else {
                _uiState.value = SettingsUiState.Error("No se pudo inicializar biometría")
            }
        }
    }
    
    fun onBiometricPromptShown() {
        _biometricPromptState.value = null
    }
    
    fun onBiometricResult(result: com.antigravity.aegis.data.security.BiometricResult) {
        when (result) {
            is com.antigravity.aegis.data.security.BiometricResult.AuthenticationSuccess -> {
                val cipher = result.result.cryptoObject?.cipher
                if (cipher != null) {
                    pendingBiometricAction?.invoke(cipher)
                    pendingBiometricAction = null
                }
            }
            is com.antigravity.aegis.data.security.BiometricResult.AuthenticationError -> {
                _uiState.value = SettingsUiState.Error("Autenticación biométrica fallida")
                pendingBiometricAction = null
            }
            else -> Unit
        }
    }
}

data class BiometricPromptConfig(
    val titleResId: Int,
    val descriptionResId: Int,
    val cryptoObject: androidx.biometric.BiometricPrompt.CryptoObject
)

sealed interface SettingsUiState {
    data object Idle : SettingsUiState
    data class Loading(val message: String) : SettingsUiState
    data class Success(val message: String) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
    data object LoggedOut : SettingsUiState
}
