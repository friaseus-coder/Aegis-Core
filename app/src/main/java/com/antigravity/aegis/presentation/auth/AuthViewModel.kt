package com.antigravity.aegis.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.usecase.FinalizeSetupUseCase
import com.antigravity.aegis.domain.usecase.InitSetupUseCase
import com.antigravity.aegis.domain.usecase.LoginWithPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.data.model.UserEntity

import com.antigravity.aegis.data.model.UserRole

import com.antigravity.aegis.domain.usecase.EnableBiometricsUseCase
import com.antigravity.aegis.domain.usecase.LoginWithBiometricsUseCase
import com.antigravity.aegis.data.security.BiometricPromptManager
import com.antigravity.aegis.data.security.BiometricResult
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.R

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val initSetupUseCase: InitSetupUseCase,
    private val finalizeSetupUseCase: FinalizeSetupUseCase,
    private val loginWithPinUseCase: LoginWithPinUseCase,
    private val enableBiometricsUseCase: EnableBiometricsUseCase,
    private val loginWithBiometricsUseCase: LoginWithBiometricsUseCase,
    private val encryptionKeyManager: EncryptionKeyManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _setupState = MutableStateFlow<SetupUiState?>(null)
    val setupState = _setupState.asStateFlow()

    // Users
    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users = _users.asStateFlow()

    private val _selectedUser = MutableStateFlow<UserEntity?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _language = MutableStateFlow("es")
    val language = _language.asStateFlow()

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable = _isBiometricAvailable.asStateFlow()
    
    // Using StateFlow for simplicity in this context, handling nulls
    private val _biometricPromptState = MutableStateFlow<BiometricPromptConfig?>(null)
    val biometricPromptState = _biometricPromptState.asStateFlow()
    
    // Login error state - contains error message or null if no error
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            authRepository.getAllUsers().collect { userList ->
                _users.value = userList
                if (userList.isEmpty()) {
                    // No users -> Needs Setup (Create Admin)
                     _authState.value = AuthState.NeedsSetup
                    if (_setupState.value == null) {
                        startSetup()
                    }
                } else {
                    // Users exist -> Locked/Select User
                    if (_authState.value == AuthState.Loading || _authState.value == AuthState.NeedsSetup) {
                         _authState.value = AuthState.Locked
                    }
                    if (_selectedUser.value == null) {
                        _selectedUser.value = userList.first()
                    }
                    checkBiometricAvailability(userList.first().id)
                }
            }
        }
    }

    fun selectUser(user: UserEntity) {
        _selectedUser.value = user
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
             checkBiometricAvailability(user.id)
        }
    }
    
    private fun checkBiometricAvailability(userId: Int) {
         _isBiometricAvailable.value = authRepository.isBiometricEnabled(userId)
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    private fun startSetup() {
        viewModelScope.launch {
            val result = initSetupUseCase()
            _setupState.value = SetupUiState(
                seedPhrase = result.seedPhrase,
                masterKey = result.masterKey
            )
        }
    }

    fun confirmSetup(pin: String) {
        val currentSetup = _setupState.value ?: return
        viewModelScope.launch {
            // Legacy flow fix: defaulting name/lang
            val result = finalizeSetupUseCase(
                name = "Admin", 
                language = "es", 
                pin = pin, 
                seedPhrase = currentSetup.seedPhrase, 
                masterKey = currentSetup.masterKey
            )
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                // Todo: Handle error
            }
        }
    }

    fun login(pin: String) {
        val user = _selectedUser.value ?: return
        _loginError.value = null // Clear any previous error
        viewModelScope.launch {
            val result = loginWithPinUseCase(user.id, pin)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _loginError.value = "PIN incorrecto"
            }
        }
    }
    
    fun clearLoginError() {
        _loginError.value = null
    }

    fun createUser(name: String, language: String, pin: String) {
        viewModelScope.launch {
            val currentSetup = _setupState.value
            
            if (currentSetup != null) {
                 // Case: Initial Setup / First User
                 val result = finalizeSetupUseCase(
                    name = name, 
                    language = language, 
                    pin = pin, 
                    seedPhrase = currentSetup.seedPhrase, 
                    masterKey = currentSetup.masterKey
                 )
                 if (result.isSuccess) {
                     _authState.value = AuthState.Authenticated
                 }
            } else {
                // Case: Adding subsequent user (Not implemented fully yet)
                // Requires Admin Auth to get MK, then createUser.
                // For now, ignoring.
            }
        }
    }

    fun logout() {
        // Clear session logic if any extra
        _authState.value = AuthState.Locked
        encryptionKeyManager.clearKey()
    }
    
    // We need a way to handle the result paired with the action.
    // Since Channel is global for the VM, one action at a time.
    private var pendingBiometricAction: ((javax.crypto.Cipher) -> Unit)? = null

    fun loginBiometric() {
        val user = _selectedUser.value ?: return
        viewModelScope.launch {
            val cipher = authRepository.getBiometricDecryptCipher(user.id)
            if (cipher != null) {
                pendingBiometricAction = { validCipher ->
                    viewModelScope.launch {
                        val result = loginWithBiometricsUseCase(user.id, validCipher)
                        if (result.isSuccess) {
                            _authState.value = AuthState.Authenticated
                        }
                    }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.biometric_login_title,
                    descriptionResId = R.string.biometric_login_desc,
                    cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            } else {
                // Biometrics not set up for this user
            }
        }
    }

    fun enableBiometric() {
        val user = _selectedUser.value ?: return
        val mk = encryptionKeyManager.getKey() ?: return // Must be logged in
        
        viewModelScope.launch {
            val cipher = authRepository.getBiometricEncryptCipher(user.id)
            if (cipher != null) {
                 pendingBiometricAction = { validCipher ->
                    viewModelScope.launch {
                         val result = enableBiometricsUseCase(user.id, mk, validCipher)
                         if (result.isSuccess) {
                             // Success
                             checkBiometricAvailability(user.id)
                         }
                    }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.biometric_enable_title,
                    descriptionResId = R.string.biometric_enable_desc,
                    cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            }
        }
    }
    
    fun onBiometricPromptShown() {
        _biometricPromptState.value = null
    }
    
    fun onBiometricResult(result: BiometricResult) {
        when (result) {
            is BiometricResult.AuthenticationSuccess -> {
                val cryptoObject = result.result.cryptoObject
                val cipher = cryptoObject?.cipher
                
                if (cipher != null) {
                    pendingBiometricAction?.invoke(cipher)
                    pendingBiometricAction = null
                }
            }
            is BiometricResult.AuthenticationError -> {
                // Todo: Handle error
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

sealed interface AuthState {
    data object Loading : AuthState
    data object NeedsSetup : AuthState
    data object Locked : AuthState
    data object Authenticated : AuthState
}

data class SetupUiState(
    val seedPhrase: List<String>,
    val masterKey: ByteArray
)
