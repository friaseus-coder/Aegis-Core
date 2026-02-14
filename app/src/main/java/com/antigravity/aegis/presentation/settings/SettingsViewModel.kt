package com.antigravity.aegis.presentation.settings

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.entity.UserRole
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.AuthRepository
import com.antigravity.aegis.domain.repository.SettingsRepository
import com.antigravity.aegis.domain.usecase.EnableBiometricsUseCase
import com.antigravity.aegis.data.local.seeder.TemplateSeeder
import com.antigravity.aegis.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val encryptionKeyManager: EncryptionKeyManager,
    private val enableBiometricsUseCase: EnableBiometricsUseCase,
    private val templateSeeder: TemplateSeeder
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
            val langName = if(language == "es") "Español" else "English"
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.msg_language_changed, langName))
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.msg_theme_changed))
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

    val users = authRepository.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.data_export_db_loading))
            val result = settingsRepository.exportDatabase(uri)
            _uiState.value = if (result.isSuccess) {
                SettingsUiState.Success(UiText.StringResource(R.string.data_export_db_success))
            } else {
                SettingsUiState.Error(UiText.StringResource(R.string.data_export_db_error, result.exceptionOrNull()?.message ?: "Unknown"))
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.data_import_db_loading))
            val result = settingsRepository.importDatabase(uri)
            _uiState.value = if (result.isSuccess) {
                // Restart App or Navigate to Login? 
                // DB is replaced, session is invalid (key might be wrong for new DB).
                logout() 
                SettingsUiState.Success(UiText.StringResource(R.string.data_import_db_success))
            } else {
                SettingsUiState.Error(UiText.StringResource(R.string.data_import_db_error, result.exceptionOrNull()?.message ?: "Unknown"))
            }
        }
    }

    fun shareBackup(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.settings_share_backup_loading))
            val result = settingsRepository.createTemporaryBackupFile()
            
            if (result.isSuccess) {
                val file = result.getOrNull()
                if (file != null) {
                    try {
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        val chooser = android.content.Intent.createChooser(intent, context.getString(R.string.settings_share_chooser_title))
                        // We need an activity context to start activity from ViewModel is bad practice usually, 
                        // but we are calling this from UI with context passed in or triggering a side effect.
                        // Better to expose Intent/Event to UI. But for simplicity here, assuming context is Activity or usign Flag New Task.
                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                        
                        _uiState.value = SettingsUiState.Idle // Reset to idle after launching
                    } catch (e: Exception) {
                        _uiState.value = SettingsUiState.Error(UiText.DynamicString(e.message ?: "Error launching share"))
                    }
                }
            } else {
                _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.settings_share_backup_error, result.exceptionOrNull()?.message ?: "Unknown"))
            }
        }
    }

    fun createNewUser(name: String, language: String = "es", pin: String, role: UserRole = UserRole.USER) {
        val masterKey = encryptionKeyManager.getKey()
        if (masterKey == null) {
             _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.auth_error_session_locked))
             return
        }
        
        // getKey() now returns raw bytes directly (not Base64 encoded)
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.general_loading))
            val result = authRepository.createUser(name, language, pin, role, null, null, masterKey)
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.auth_success_user_created))
            } else {
                 _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.auth_error_user_create_failed, result.exceptionOrNull()?.message ?: "Unknown"))
            }
        }
    }

    fun updateUserRole(userId: Int, role: UserRole) {
        viewModelScope.launch {
            val result = authRepository.updateUserRole(userId, role)
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.settings_role_update_success))
            } else {
                _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.settings_role_update_error))
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
        _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.general_loading))
        
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
                            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.auth_success_biometric_enabled))
                        } else {
                            _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.auth_success_biometric_enable_error))
                        }
                    }
                }
                _biometricPromptState.value = BiometricPromptConfig(
                    titleResId = R.string.auth_login_biometric_enable_title,
                    descriptionResId = R.string.auth_login_biometric_enable_desc,
                    cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            } else {
                _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.auth_error_biometric_generic))
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
                _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.auth_error_biometric_generic))
                pendingBiometricAction = null
            }
            else -> Unit
        }
    }

    fun updateCompanyConfig(
        name: String,
        address: String,
        postalCode: String,
        city: String,
        province: String,
        dniCif: String
    ) {
        viewModelScope.launch {
            val currentConfig = userConfig.value
            // CRÍTICO: Asegurar que siempre usamos id = 1 para que Room pueda actualizar correctamente
            val newConfig = (currentConfig ?: com.antigravity.aegis.data.local.entity.UserConfig()).copy(
                id = 1, // Forzar id = 1 siempre
                companyName = name,
                companyAddress = address,
                companyPostalCode = postalCode,
                companyCity = city,
                companyProvince = province,
                companyDniCif = dniCif
            )
            settingsRepository.insertOrUpdateConfig(newConfig)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.general_success))
        }
    }

    fun updateCompanyLogo(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.settings_logo_saving))
            val result = settingsRepository.saveImageToInternalStorage(uri)
            if (result.isSuccess) {
                val path = result.getOrNull()
                val currentConfig = userConfig.value
                if (currentConfig != null && path != null) {
                     val newConfig = currentConfig.copy(companyLogoUri = path)
                     settingsRepository.insertOrUpdateConfig(newConfig)
                     _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.settings_logo_updated))
                }
            } else {
                _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.general_error_prefix, result.exceptionOrNull()?.message ?: "Unknown"))
            }
        }
    }

    fun deleteCompanyLogo() {
        viewModelScope.launch {
            val currentConfig = userConfig.value ?: return@launch
            // Optionally delete the file from internal storage if needed, but clearing URI is enough for now.
            val newConfig = currentConfig.copy(companyLogoUri = null)
            settingsRepository.insertOrUpdateConfig(newConfig)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.settings_logo_deleted))
        }
    }
    
    // ========== Module Customization ==========
    
    /**
     * Obtiene la lista de módulos con su configuración actual (visibilidad y orden)
     */
    fun getModuleConfigurations(): kotlinx.coroutines.flow.Flow<List<com.antigravity.aegis.domain.model.ModuleConfig>> {
        return userConfig.map { config ->
            val defaultModules = getDefaultModules()
            
            if (config == null || config.moduleOrder.isEmpty()) {
                // Sin configuración, devolver módulos por defecto
                defaultModules
            } else {
                // Parsear configuración guardada
                val orderList = try {
                    org.json.JSONArray(config.moduleOrder).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                } catch (e: Exception) {
                    emptyList()
                }
                
                val hiddenList = try {
                    org.json.JSONArray(config.hiddenModules).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                } catch (e: Exception) {
                    emptyList()
                }
                
                // Aplicar configuración
                val moduleMap = defaultModules.associateBy { it.id }
                orderList.mapIndexedNotNull { index, id ->
                    moduleMap[id]?.copy(
                        isVisible = id !in hiddenList,
                        order = index
                    )
                }
            }
        }
    }
    
    /**
     * Actualiza la visibilidad de un módulo
     */
    fun updateModuleVisibility(moduleId: String, isVisible: Boolean) {
        viewModelScope.launch {
            val currentConfig = userConfig.value ?: com.antigravity.aegis.data.local.entity.UserConfig()
            
            val hiddenList = try {
                org.json.JSONArray(currentConfig.hiddenModules).let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }.toMutableList()
                }
            } catch (e: Exception) {
                mutableListOf()
            }
            
            if (isVisible) {
                hiddenList.remove(moduleId)
            } else {
                if (moduleId !in hiddenList) hiddenList.add(moduleId)
            }
            
            val newHiddenJson = org.json.JSONArray(hiddenList).toString()
            val newConfig = currentConfig.copy(
                id = 1,
                hiddenModules = newHiddenJson
            )
            
            settingsRepository.insertOrUpdateConfig(newConfig)
        }
    }
    
    /**
     * Actualiza el orden de los módulos
     */
    fun updateModuleOrder(newOrder: List<String>) {
        viewModelScope.launch {
            val currentConfig = userConfig.value ?: com.antigravity.aegis.data.local.entity.UserConfig()
            
            val orderJson = org.json.JSONArray(newOrder).toString()
            val newConfig = currentConfig.copy(
                id = 1,
                moduleOrder = orderJson
            )
            
            settingsRepository.insertOrUpdateConfig(newConfig)
        }
    }
    
    /**
     * Restaura la configuración de módulos a valores predeterminados
     */
    fun restoreDefaultModules() {
        viewModelScope.launch {
            val currentConfig = userConfig.value ?: com.antigravity.aegis.data.local.entity.UserConfig()
            
            val newConfig = currentConfig.copy(
                id = 1,
                moduleOrder = "",
                hiddenModules = ""
            )
            
            settingsRepository.insertOrUpdateConfig(newConfig)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.module_customization_restored))
        }
    }
    
    /**
     * Devuelve la lista de módulos por defecto
     */
    private fun getDefaultModules(): List<com.antigravity.aegis.domain.model.ModuleConfig> {
        return listOf(
            com.antigravity.aegis.domain.model.ModuleConfig("projects", R.string.module_id_projects, true, 0),
            com.antigravity.aegis.domain.model.ModuleConfig("work_reports", R.string.module_id_work_reports, true, 1),
            com.antigravity.aegis.domain.model.ModuleConfig("budgets", R.string.module_id_budgets, true, 2),
            com.antigravity.aegis.domain.model.ModuleConfig("expenses", R.string.module_id_expenses, true, 3),
            com.antigravity.aegis.domain.model.ModuleConfig("inventory", R.string.module_id_inventory, true, 4),
            com.antigravity.aegis.domain.model.ModuleConfig("time_control", R.string.module_id_time_control, true, 5),
            com.antigravity.aegis.domain.model.ModuleConfig("clients", R.string.module_id_clients, true, 6),
            com.antigravity.aegis.domain.model.ModuleConfig("mileage", R.string.module_id_mileage, true, 7)
        )
    }

    fun loadDefaultTemplates() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.general_loading))
            try {
                templateSeeder.seedTemplates()
                _uiState.value = SettingsUiState.Success(UiText.DynamicString("Plantillas predefinidas cargadas correctamente."))
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(UiText.DynamicString("Error al cargar plantillas: ${e.message}"))
            }
        }
    }
}

data class BiometricPromptConfig(
    val titleResId: Int,
    val descriptionResId: Int,
    val cryptoObject: androidx.biometric.BiometricPrompt.CryptoObject
)

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @androidx.annotation.StringRes val resId: Int,
        vararg val args: Any
    ) : UiText
    
    fun asString(context: android.content.Context): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}

sealed interface SettingsUiState {
    data object Idle : SettingsUiState
    data class Loading(val message: UiText) : SettingsUiState
    data class Success(val message: UiText) : SettingsUiState
    data class Error(val message: UiText) : SettingsUiState
    data object LoggedOut : SettingsUiState
}
