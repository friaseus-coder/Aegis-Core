package com.antigravity.aegis.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.entity.ActiveRole
import com.antigravity.aegis.data.local.entity.EntityType
import com.antigravity.aegis.data.local.entity.UserConfig
import com.antigravity.aegis.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userConfig: StateFlow<UserConfig?> = settingsRepository.getUserConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _enabledModules = MutableStateFlow<List<AppModule>>(emptyList())
    val enabledModules: StateFlow<List<AppModule>> = _enabledModules

    init {
        viewModelScope.launch {
            userConfig.collect { config ->
                config?.let {
                    updateEnabledModules(it)
                }
            }
        }
    }

    private fun updateEnabledModules(config: UserConfig) {
        val allModules = listOf(
            AppModule.CRM,
            AppModule.FIELD_SERVICE,
            AppModule.BUDGETS,
            AppModule.EXPENSES,
            AppModule.INVENTORY,
            AppModule.MILEAGE
        )

        val filtered = if (config.activeRole == ActiveRole.TRABAJADOR) {
            // Operativa: Proyectos (CRM), Partes (Full CRM access needed for Projects?), Info says:
            // "Vista Operativa (Trabajador): Filtra y muestra solo los módulos necesarios (Proyectos, Partes de Trabajo, Control Horario y Kilometraje)."
            // Assuming FIELD_SERVICE is Partes. CRM has Projects. MILEAGE is Kilometraje.
            // Control Horario is not yet a separate module, maybe part of Field Service?
            // Excluding Budgets, Expenses (Gastos), Inventory (Inventario)?? 
            // Wait, "CRM" in MainMenuScreen title is "Project Hub".
            // Expenses (Gastos) should be excluded? Requirement: "Muestra todos los módulos (incluyendo Gastos, Presupuestos e Inventario)" for Autonomo.
            // So Trabajador = CRM (Proyectos), FIELD_SERVICE, MILEAGE.
            listOf(AppModule.CRM, AppModule.FIELD_SERVICE, AppModule.MILEAGE)
        } else {
            // AUTONOMO: All modules
            allModules
        }
        _enabledModules.value = filtered
    }

    fun updateProfile(
        name: String,
        type: EntityType,
        role: ActiveRole,
        dualMode: Boolean,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            val internalPath = imageUri?.let { uri ->
                copyImageToInternalStorage(uri)
            }
            
            // We need a method in Repository to update the config. 
            // Currently UserConfigDao has generic insert/replace.
            val current = userConfig.value ?: UserConfig()
            val updated = current.copy(
                titularName = name,
                entityType = type,
                activeRole = role,
                isDualModeEnabled = dualMode,
                profileImageUri = internalPath ?: current.profileImageUri
            )
            settingsRepository.insertOrUpdateConfig(updated)
        }
    }

    fun toggleRole(newRole: ActiveRole) {
        viewModelScope.launch {
            val current = userConfig.value ?: return@launch
            val updated = current.copy(activeRole = newRole)
            settingsRepository.insertOrUpdateConfig(updated)
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

enum class AppModule {
    CRM, FIELD_SERVICE, BUDGETS, EXPENSES, INVENTORY, MILEAGE
}
