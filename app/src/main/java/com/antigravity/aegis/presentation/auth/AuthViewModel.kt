package com.antigravity.aegis.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.usecase.FinalizeSetupUseCase
import com.antigravity.aegis.domain.usecase.InitSetupUseCase
import com.antigravity.aegis.domain.usecase.LoginWithPinUseCase
import com.antigravity.aegis.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.data.local.entity.UserEntity

import com.antigravity.aegis.data.local.entity.UserRole

import com.antigravity.aegis.domain.usecase.EnableBiometricsUseCase
import com.antigravity.aegis.domain.usecase.LoginWithBiometricsUseCase
import com.antigravity.aegis.data.security.BiometricPromptManager
import com.antigravity.aegis.data.security.BiometricResult
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.presentation.util.UiText
import com.antigravity.aegis.R

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val initSetupUseCase: InitSetupUseCase,
    private val finalizeSetupUseCase: FinalizeSetupUseCase,
    private val loginWithPinUseCase: LoginWithPinUseCase,
    private val enableBiometricsUseCase: EnableBiometricsUseCase,
    private val loginWithBiometricsUseCase: LoginWithBiometricsUseCase,
    private val encryptionKeyManager: EncryptionKeyManager,
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository
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

    private val _language = MutableStateFlow(com.antigravity.aegis.presentation.util.LanguageUtils.getDefaultPlatformLanguage())
    val language = _language.asStateFlow()

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable = _isBiometricAvailable.asStateFlow()
    
    // Using StateFlow for simplicity in this context, handling nulls
    private val _biometricPromptState = MutableStateFlow<BiometricPromptConfig?>(null)
    val biometricPromptState = _biometricPromptState.asStateFlow()
    
    // Login error state - contains error message or null if no error
    private val _loginError = MutableStateFlow<UiText?>(null)
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
        viewModelScope.launch {
            try {
                // Ensure config exists first by collecting flow once
                val config = settingsRepository.getUserConfig().first()
                if (config == null) {
                    // Create default if missing
                     settingsRepository.insertOrUpdateConfig(com.antigravity.aegis.data.local.entity.UserConfig(language = lang))
                } else {
                    settingsRepository.updateLanguage(lang)
                }
            } catch (e: Exception) {
                // Log? Set local only?
            }
        }
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

    fun confirmSetup(name: String, language: String, pin: String, role: UserRole, email: String?, phone: String?) {
        val currentSetup = _setupState.value ?: return
        viewModelScope.launch {
            val result = finalizeSetupUseCase(
                name = name, 
                language = language, 
                pin = pin, 
                role = role,
                email = email,
                phone = phone,
                seedPhrase = currentSetup.seedPhrase, 
                masterKey = currentSetup.masterKey
            )
            when (result) {
                is Result.Success -> _authState.value = AuthState.Authenticated
                is Result.Error -> { /* Todo: Handle error */ }
            }
        }
    }
    
    fun recoverWithWords(words: List<String>) {
        viewModelScope.launch {
            when (val result = authRepository.recoverWithSeed(words)) {
                is Result.Success -> {
                    encryptionKeyManager.setMasterKey(result.data)
                    _authState.value = AuthState.RecoverySuccess
                }
                is Result.Error -> {
                    _loginError.value = UiText.StringResource(R.string.auth_error_invalid_seed)
                }
            }
        }
    }

    fun recoverWithEmailPhone(email: String, phone: String) {
        val user = _selectedUser.value ?: return
        viewModelScope.launch {
            when (val result = authRepository.recoverWithEmailPhone(user.id, email, phone)) {
                is Result.Success -> {
                    encryptionKeyManager.setMasterKey(result.data)
                    _authState.value = AuthState.RecoverySuccess
                }
                is Result.Error -> {
                    _loginError.value = UiText.StringResource(R.string.auth_error_invalid_credentials)
                }
            }
        }
    }

    fun login(pin: String) {
        val user = _selectedUser.value ?: return
        _loginError.value = null // Clear any previous error
        viewModelScope.launch {
            when (val result = loginWithPinUseCase(user.id, pin)) {
                is Result.Success -> {
                    if (user.forcePinChange) {
                        _authState.value = AuthState.RecoverySuccess
                    } else {
                        _authState.value = AuthState.Authenticated
                    }
                }
                is Result.Error -> {
                    _loginError.value = UiText.StringResource(R.string.auth_error_pin_incorrect)
                }
            }
        }
    }
    
    fun changePin(newPin: String) {
        val user = _selectedUser.value ?: return
        val mk = encryptionKeyManager.getKey() ?: return
        
        viewModelScope.launch {
            when (authRepository.updatePin(user.id, newPin, mk)) {
                is Result.Success -> _authState.value = AuthState.Authenticated
                is Result.Error -> { /* Handle error */ }
            }
        }
    }
    
    fun clearLoginError() {
        _loginError.value = null
    }

    fun createUser(name: String, language: String, pin: String, email: String?, phone: String?) {
        viewModelScope.launch {
            val currentSetup = _setupState.value
            
            if (currentSetup != null) {
                 // Case: Initial Setup / First User
                 when (val result = finalizeSetupUseCase(
                    name = name,
                    language = language,
                    pin = pin,
                    role = com.antigravity.aegis.data.local.entity.UserRole.ADMIN,
                    email = email,
                    phone = phone,
                    seedPhrase = currentSetup.seedPhrase,
                    masterKey = currentSetup.masterKey
                 )) {
                     is Result.Success -> _authState.value = AuthState.Authenticated
                     is Result.Error -> { /* Handle error */ }
                 }
            } else {
                // Case: Adding subsequent user
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
                        when (loginWithBiometricsUseCase(user.id, validCipher)) {
                            is Result.Success -> _authState.value = AuthState.Authenticated
                            is Result.Error -> { /* Biometric failed silently; cipher was valid but key decrypt failed */ }
                        }
                    }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.auth_login_biometric_prompt_title,
                    descriptionResId = R.string.auth_login_biometric_prompt_desc,
                    cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            } else {
                // Biometrics not set up for this user
                _loginError.value = UiText.StringResource(R.string.auth_error_biometric_unavailable)
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
                         when (enableBiometricsUseCase(user.id, mk, validCipher)) {
                             is Result.Success -> checkBiometricAvailability(user.id)
                             is Result.Error -> { /* Handle silently */ }
                         }
                     }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.auth_login_biometric_enable_title,
                    descriptionResId = R.string.auth_login_biometric_enable_desc,
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
                _loginError.value = UiText.StringResource(R.string.auth_error_biometric_generic, result.error)
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
    data object RecoverySuccess : AuthState
}

data class SetupUiState(
    val seedPhrase: List<String>,
    val masterKey: ByteArray
)
