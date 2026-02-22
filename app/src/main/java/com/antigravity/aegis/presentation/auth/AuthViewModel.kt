package com.antigravity.aegis.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.presentation.util.UiText

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _setupState = MutableStateFlow<SetupUiState?>(null)
    val setupState = _setupState.asStateFlow()

    private val _language = MutableStateFlow(com.antigravity.aegis.presentation.util.LanguageUtils.getDefaultPlatformLanguage())
    val language = _language.asStateFlow()
    
    private val _loginError = MutableStateFlow<UiText?>(null)
    val loginError = _loginError.asStateFlow()

    init {
        checkSetupStatus()
    }

    private fun checkSetupStatus() {
        viewModelScope.launch {
            if (authRepository.isSetupDone()) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.NeedsSetup
                startSetup()
            }
        }
    }

    private fun startSetup() {
        viewModelScope.launch {
            _setupState.value = SetupUiState(
                masterKey = ByteArray(0) // Obsoleto, ya no se usa criptografía para datos offline
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
        viewModelScope.launch {
            authRepository.markSetupComplete()
            _authState.value = AuthState.Authenticated
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun login() {
        _authState.value = AuthState.Authenticated
    }

    fun logout() {
        _authState.value = AuthState.Locked
    }
}

sealed interface AuthState {
    data object Loading : AuthState
    data object NeedsSetup : AuthState
    data object Locked : AuthState
    data object Authenticated : AuthState
}

data class SetupUiState(
    val masterKey: ByteArray
)
