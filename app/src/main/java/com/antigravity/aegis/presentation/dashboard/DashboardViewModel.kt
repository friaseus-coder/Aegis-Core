package com.antigravity.aegis.presentation.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.antigravity.aegis.presentation.navigation.Screen
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog = _showBackupDialog.asStateFlow()

    private val _autoBackupStatus = MutableStateFlow<String?>(null)
    val autoBackupStatus = _autoBackupStatus.asStateFlow()

    init {
        checkBackupConfiguration()
    }

    private fun checkBackupConfiguration() {
        viewModelScope.launch {
            // We expect UserConfig to exist at this point (user logged in)
            val config = settingsRepository.getUserConfig().firstOrNull()
            
            if (config != null) {
                if (config.backupLocationUri == null) {
                    _showBackupDialog.value = true
                } else {
                    // Backup is configured. Check if we should backup.
                    // For now, per requirements, we backup on start (or check if possible).
                    performBackup(config)
                }
            } else {
                // If config is null for some reason, we avoid showing dialog until it loads...
                // But Flow might emit null initially?
                // Depending on Dao implementation.
            }
        }
    }

    fun onBackupLocationSelected(uri: Uri) {
        viewModelScope.launch {
            val persistResult = settingsRepository.persistBackupUri(uri)
            if (persistResult.isSuccess) {
                _showBackupDialog.value = false
                
                // Perform the first backup immediately
                 val config = settingsRepository.getUserConfig().firstOrNull()
                 if (config != null) {
                     performBackup(config)
                 }
            } else {
                _autoBackupStatus.value = "Error guardando permisos: ${persistResult.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun onDismissBackupDialog() {
        // Decide logic: Force user? Or allow dismiss?
        // Requirement says "it has to ask you...".
        // Let's allow dismiss but it will ask again next time? 
        // For now, making it non-dismissable in UI or simply doing nothing here.
        // User can't work easily without it if we enforce blocking dialog.
        // Let's hide it for this session if user insists (Back press?), but dialog has properties to prevent dismiss.
        // The dialog UI implemention I wrote has dismissOnBackPress = false.
        // So user MUST choose.
    }
    
    private suspend fun performBackup(userConfig: com.antigravity.aegis.data.local.entity.UserConfig) {
        // Maybe check timestamp to not spam backup on every rotation/nav?
        // But ViewModel survives config changes.
        // Only on app restart.
        
        val result = settingsRepository.performAutoBackup(userConfig)
        if (result.isSuccess) {
            _autoBackupStatus.value = "Copia de seguridad guardada: ${result.getOrNull()}"
        } else {
             _autoBackupStatus.value = "Error backup automático: ${result.exceptionOrNull()?.message}"
        }
    }

    fun clearBackupStatus() {
        _autoBackupStatus.value = null
    }

    // --- Module Customization ---

    val configuredModules: Flow<List<ModuleData>> = 
        settingsRepository.getUserConfig().map { config ->
            val defaultModules = listOf(
                ModuleData("Proyectos", Icons.Filled.Work, Screen.Projects.route),
                ModuleData("Presupuestos", Icons.Filled.Money, Screen.Budgets.route),
                ModuleData("Gastos", Icons.Filled.Money, Screen.Expenses.route),
                ModuleData("Inventario", Icons.Filled.Inventory, Screen.Inventory.route),
                ModuleData("Control Horario", Icons.Filled.Schedule, Screen.TimeControl.route),
                ModuleData("Clientes", Icons.Filled.Person, Screen.Clients.route),
                ModuleData("Kilometraje", Icons.Filled.Map, Screen.Mileage.route)
            )

            // Map default modules to their string IDs used in config
            val moduleIdMap = mapOf(
                "projects" to "Proyectos",
                "budgets" to "Presupuestos",
                "expenses" to "Gastos",
                "inventory" to "Inventario",
                "time_control" to "Control Horario",
                "clients" to "Clientes",
                "mileage" to "Kilometraje"
            )

            // Reverse map to get module by ID easily
            val defaultModulesById = defaultModules.associateBy { module ->
                moduleIdMap.entries.firstOrNull { it.value == module.name }?.key ?: ""
            }

            val hiddenList = try {
                if (config == null || config.hiddenModules.isBlank()) {
                    emptyList<String>()
                } else {
                    org.json.JSONArray(config.hiddenModules).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                }
            } catch (e: Exception) {
                emptyList<String>()
            }

            if (config == null || config.moduleOrder.isBlank()) {
                // Return default order, but filter out hidden
                defaultModules.filter { module ->
                    val id = moduleIdMap.entries.firstOrNull { it.value == module.name }?.key ?: ""
                    id !in hiddenList
                }
            } else {
                val orderList = try {
                    org.json.JSONArray(config.moduleOrder).let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                } catch (e: Exception) {
                    emptyList<String>()
                }

                val orderedModules = mutableListOf<ModuleData>()
                val remainingMap = defaultModulesById.toMutableMap()

                orderList.forEach { id ->
                    remainingMap[id]?.let { module ->
                        if (id !in hiddenList) orderedModules.add(module)
                        remainingMap.remove(id)
                    }
                }

                remainingMap.values.forEach { module ->
                    val id = moduleIdMap.entries.firstOrNull { it.value == module.name }?.key ?: ""
                    if (id !in hiddenList) orderedModules.add(module)
                }

                orderedModules
            }
        }
}
