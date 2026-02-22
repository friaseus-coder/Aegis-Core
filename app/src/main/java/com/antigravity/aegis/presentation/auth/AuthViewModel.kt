package com.antigravity.aegis.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.usecase.FinalizeSetupUseCase
import com.antigravity.aegis.domain.usecase.InitSetupUseCase
import com.antigravity.aegis.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.data.security.BiometricPromptManager
import com.antigravity.aegis.data.security.BiometricResult
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.presentation.util.UiText
import com.antigravity.aegis.R
import javax.crypto.Cipher

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val initSetupUseCase: InitSetupUseCase,
    private val finalizeSetupUseCase: FinalizeSetupUseCase,
    private val encryptionKeyManager: EncryptionKeyManager,
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _setupState = MutableStateFlow<SetupUiState?>(null)
    val setupState = _setupState.asStateFlow()

    private val _language = MutableStateFlow(com.antigravity.aegis.presentation.util.LanguageUtils.getDefaultPlatformLanguage())
    val language = _language.asStateFlow()
    
    // Configura el BiometricPrompt de OS
    private val _biometricPromptState = MutableStateFlow<BiometricPromptConfig?>(null)
    val biometricPromptState = _biometricPromptState.asStateFlow()
    
    private val _loginError = MutableStateFlow<UiText?>(null)
    val loginError = _loginError.asStateFlow()

    // Tracking the pending OS Action
    private var pendingOsAction: (() -> Unit)? = null

    init {
        checkSetupStatus()
    }

    private fun checkSetupStatus() {
        viewModelScope.launch {
            if (authRepository.isSetupDone()) {
                if (!authRepository.hasOsCredentials()) {
                    _loginError.value = UiText.DynamicString("Se detectó una bóveda de versión anterior incompatible. Por favor, ve a Ajustes de la app o Android para borrar los datos o desinstala esta app.")
                    _authState.value = AuthState.Locked
                } else {
                    _authState.value = AuthState.Locked
                    triggerOsLock()
                }
            } else {
                _authState.value = AuthState.NeedsSetup
                startSetup()
            }
        }
    }

    private fun startSetup() {
        viewModelScope.launch {
            val result = initSetupUseCase()
            _setupState.value = SetupUiState(
                masterKey = result.masterKey
            )
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            try {
                val config = settingsRepository.getUserConfig().first()
                if (config == null) {
                     settingsRepository.insertOrUpdateConfig(com.antigravity.aegis.data.local.entity.UserConfig(language = lang))
                } else {
                    settingsRepository.updateLanguage(lang)
                }
            } catch (e: Exception) {}
        }
    }

    fun confirmSetup() {
        val currentSetup = _setupState.value ?: return
        viewModelScope.launch {
            pendingOsAction = {
                viewModelScope.launch {
                    val validCipher = authRepository.getOsEncryptCipher()
                    if (validCipher != null) {
                        val result = finalizeSetupUseCase(
                            masterKey = currentSetup.masterKey,
                            cipher = validCipher
                        )
                        when (result) {
                            is Result.Success -> _authState.value = AuthState.Authenticated
                            is Result.Error -> _loginError.value = UiText.StringResource(R.string.general_error_prefix, result.exception.message ?: "")
                        }
                    } else {
                        _loginError.value = UiText.StringResource(R.string.auth_error_biometric_unavailable)
                    }
                }
            }
            requestOsAuthentication(R.string.auth_login_biometric_enable_title, R.string.auth_login_biometric_enable_desc)
        }
    }
    

    fun triggerOsLock() {
        viewModelScope.launch {
            pendingOsAction = {
                viewModelScope.launch {
                    val validCipher = authRepository.getOsDecryptCipher()
                    if (validCipher != null) {
                        when (val result = authRepository.loginWithOs(validCipher)) {
                            is Result.Success -> {
                                encryptionKeyManager.setMasterKey(result.data)
                                _authState.value = AuthState.Authenticated
                            }
                            is Result.Error -> {
                                _loginError.value = UiText.StringResource(R.string.auth_error_pin_incorrect)
                            }
                        }
                    } else {
                        _loginError.value = UiText.StringResource(R.string.auth_error_biometric_unavailable)
                    }
                }
            }
            requestOsAuthentication(R.string.auth_login_biometric_prompt_title, R.string.auth_login_biometric_prompt_desc)
        }
    }
    
    private fun requestOsAuthentication(title: Int, desc: Int) {
         _biometricPromptState.value = BiometricPromptConfig(
            titleResId = title,
            descriptionResId = desc,
            cryptoObject = null // NO USAR CRYPTOOBJECT, DEPENDER DE TIMEOUT!
        )
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun logout() {
        _authState.value = AuthState.Locked
        encryptionKeyManager.clearKey()
    }

    fun onBiometricPromptShown() {
        _biometricPromptState.value = null
    }
    
    fun onBiometricResult(result: BiometricResult) {
        when (result) {
            is BiometricResult.AuthenticationSuccess -> {
                pendingOsAction?.invoke()
                pendingOsAction = null
            }
            is BiometricResult.AuthenticationError -> {
                 _loginError.value = UiText.StringResource(R.string.auth_error_biometric_generic, result.error)
                 pendingOsAction = null
            }
            else -> Unit
        }
    }
}

data class BiometricPromptConfig(
    val titleResId: Int,
    val descriptionResId: Int,
    val cryptoObject: androidx.biometric.BiometricPrompt.CryptoObject?
)

sealed interface AuthState {
    data object Loading : AuthState
    data object NeedsSetup : AuthState
    data object Locked : AuthState
    data object Authenticated : AuthState
}

data class SetupUiState(
    val masterKey: ByteArray
)
