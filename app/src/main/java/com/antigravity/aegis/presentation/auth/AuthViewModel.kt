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

    // Users
    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users = _users.asStateFlow()

    private val _selectedUser = MutableStateFlow<UserEntity?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _language = MutableStateFlow("es")
    val language = _language.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
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
                }
            }
        }
    }

    fun selectUser(user: UserEntity) {
        _selectedUser.value = user
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
        viewModelScope.launch {
            val result = loginWithPinUseCase(user.id, pin)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                // Todo: Show error (Shake animation etc)
            }
        }
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
