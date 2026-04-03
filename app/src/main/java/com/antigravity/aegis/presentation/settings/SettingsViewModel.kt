package com.antigravity.aegis.presentation.settings

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.security.EncryptionKeyManager
import com.antigravity.aegis.domain.repository.SettingsRepository
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
    private val settingsRepository: SettingsRepository,
    private val encryptionKeyManager: EncryptionKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()
    

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

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.updateCurrency(currency)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.general_success))
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.msg_theme_changed))
        }
    }

    fun updateDefaultTaxPercent(percent: Double) {
        viewModelScope.launch {
            val currentConfig = userConfig.value ?: com.antigravity.aegis.data.local.entity.UserConfig()
            val newConfig = currentConfig.copy(id = 1, defaultTaxPercent = percent)
            settingsRepository.insertOrUpdateConfig(newConfig)
            _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.general_success))
        }
    }
    

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.data_export_db_loading))
            when (val result = settingsRepository.exportDatabase(uri)) {
                is com.antigravity.aegis.domain.util.Result.Success -> _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.data_export_db_success))
                is com.antigravity.aegis.domain.util.Result.Error -> _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.data_export_db_error, result.exception.message ?: "Unknown"))
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.data_import_db_loading))
            when (val result = settingsRepository.importDatabase(uri)) {
                is com.antigravity.aegis.domain.util.Result.Success -> {
                    logout()
                    _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.data_import_db_success))
                }
                is com.antigravity.aegis.domain.util.Result.Error -> _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.data_import_db_error, result.exception.message ?: "Unknown"))
            }
        }
    }

    fun shareBackup(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading(UiText.StringResource(R.string.settings_share_backup_loading))
            when (val result = settingsRepository.createTemporaryBackupFile()) {
                is com.antigravity.aegis.domain.util.Result.Success -> {
                    val file = result.data
                    try {
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = android.content.Intent.createChooser(intent, context.getString(R.string.settings_share_chooser_title))
                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                        _uiState.value = SettingsUiState.Idle
                    } catch (e: Exception) {
                        _uiState.value = SettingsUiState.Error(UiText.DynamicString(e.message ?: "Error launching share"))
                    }
                }
                is com.antigravity.aegis.domain.util.Result.Error -> {
                    _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.settings_share_backup_error, result.exception.message ?: "Unknown"))
                }
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
            when (val result = settingsRepository.saveImageToInternalStorage(uri)) {
                is com.antigravity.aegis.domain.util.Result.Success -> {
                    val path = result.data
                    val currentConfig = userConfig.value
                    if (currentConfig != null) {
                        val newConfig = currentConfig.copy(companyLogoUri = path)
                        settingsRepository.insertOrUpdateConfig(newConfig)
                        _uiState.value = SettingsUiState.Success(UiText.StringResource(R.string.settings_logo_updated))
                    }
                }
                is com.antigravity.aegis.domain.util.Result.Error -> {
                    _uiState.value = SettingsUiState.Error(UiText.StringResource(R.string.general_error_prefix, result.exception.message ?: "Unknown"))
                }
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
            
            val hiddenList = try {
                if (config == null || config.hiddenModules.isBlank()) {
                    emptyList()
                } else {
                    org.json.JSONArray(config.hiddenModules).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }

            if (config == null || config.moduleOrder.isBlank()) {
                // Sin orden personalizado, devolver módulos por defecto aplicando visibilidad
                defaultModules.map { it.copy(isVisible = it.id !in hiddenList) }
            } else {
                // Parsear configuración guardada de orden
                val orderList = try {
                    org.json.JSONArray(config.moduleOrder).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                } catch (e: Exception) {
                    emptyList<String>()
                }
                
                // Aplicar configuración de orden y visibilidad
                val moduleMap = defaultModules.associateBy { it.id }.toMutableMap()
                val orderedModules = mutableListOf<com.antigravity.aegis.domain.model.ModuleConfig>()
                
                // 1. Añadir los módulos usando el orden guardado
                orderList.forEachIndexed { index, id ->
                    moduleMap[id]?.let { module ->
                        orderedModules.add(module.copy(
                            isVisible = id !in hiddenList,
                            order = index
                        ))
                        moduleMap.remove(id) // Marcar como procesado
                    }
                }
                
                // 2. Añadir cualquier módulo nuevo (por ej tras una actualización) que no estuviera en el orden guardado
                moduleMap.values.forEach { module ->
                    orderedModules.add(module.copy(
                        isVisible = module.id !in hiddenList,
                        order = orderedModules.size
                    ))
                }
                
                orderedModules
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
}


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
