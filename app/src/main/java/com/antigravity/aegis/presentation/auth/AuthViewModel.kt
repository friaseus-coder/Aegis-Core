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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val initSetupUseCase: InitSetupUseCase,
    private val finalizeSetupUseCase: FinalizeSetupUseCase,
    private val loginWithPinUseCase: LoginWithPinUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _setupState = MutableStateFlow<SetupUiState?>(null)
    val setupState = _setupState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (authRepository.isSetupDone()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.NeedsSetup
            startSetup()
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

    fun confirmSetup(pin: String) {
        val currentSetup = _setupState.value ?: return
        viewModelScope.launch {
            val result = finalizeSetupUseCase(pin, currentSetup.seedPhrase, currentSetup.masterKey)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                // Handle error
            }
        }
    }

    fun login(pin: String) {
        viewModelScope.launch {
            val result = loginWithPinUseCase(pin)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                // Show error
            }
        }
    }
}

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
