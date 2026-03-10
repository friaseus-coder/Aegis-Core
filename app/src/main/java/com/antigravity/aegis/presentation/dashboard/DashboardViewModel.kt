package com.antigravity.aegis.presentation.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.repository.SettingsRepository
import com.antigravity.aegis.domain.util.Result as DomainResult
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

    sealed class BackupStatus {
        data class Success(val fileName: String) : BackupStatus()
        data class Error(val message: String?) : BackupStatus()
        data class PermissionError(val message: String?) : BackupStatus()
    }

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog = _showBackupDialog.asStateFlow()

    private val _autoBackupStatus = MutableStateFlow<BackupStatus?>(null)
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
            when (val persistResult = settingsRepository.persistBackupUri(uri)) {
                is DomainResult.Success -> {
                    _showBackupDialog.value = false
                    val config = settingsRepository.getUserConfig().firstOrNull()
                    if (config != null) performBackup(config)
                }
                is DomainResult.Error -> {
                    _autoBackupStatus.value = BackupStatus.PermissionError(persistResult.exception.message)
                }
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
        when (val result = settingsRepository.performAutoBackup(userConfig)) {
            is DomainResult.Success -> _autoBackupStatus.value = BackupStatus.Success(result.data)
            is DomainResult.Error -> _autoBackupStatus.value = BackupStatus.Error(result.exception.message)
        }
    }

    fun clearBackupStatus() {
        _autoBackupStatus.value = null
    }

    // --- Module Customization ---

    val configuredModules: Flow<List<ModuleData>> = 
        settingsRepository.getUserConfig().map { config ->
            val defaultModules = listOf(
                ModuleData(com.antigravity.aegis.R.string.module_id_projects, Icons.Filled.Work, Screen.Projects.route),
                ModuleData(com.antigravity.aegis.R.string.module_id_budgets, Icons.Filled.Contacts, Screen.Budgets.route),
                ModuleData(com.antigravity.aegis.R.string.module_id_expenses, Icons.Filled.Money, Screen.Expenses.route),
                ModuleData(com.antigravity.aegis.R.string.module_id_time_control, Icons.Filled.Schedule, Screen.TimeControl.route),
                ModuleData(com.antigravity.aegis.R.string.module_id_clients, Icons.Filled.Person, Screen.Clients.route),
                ModuleData(com.antigravity.aegis.R.string.module_id_mileage, Icons.Filled.Map, Screen.Mileage.route)
            )

            // Map default modules to their string IDs used in config
            val moduleIdMap = mapOf(
                "projects" to com.antigravity.aegis.R.string.module_id_projects,
                "budgets" to com.antigravity.aegis.R.string.module_id_budgets,
                "expenses" to com.antigravity.aegis.R.string.module_id_expenses,
                "time_control" to com.antigravity.aegis.R.string.module_id_time_control,
                "clients" to com.antigravity.aegis.R.string.module_id_clients,
                "mileage" to com.antigravity.aegis.R.string.module_id_mileage
            )

            // Reverse map to get module by ID easily
            val defaultModulesById = defaultModules.associateBy { module ->
                moduleIdMap.entries.firstOrNull { it.value == module.titleResId }?.key ?: ""
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
                    val id = moduleIdMap.entries.firstOrNull { it.value == module.titleResId }?.key ?: ""
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
                    val id = moduleIdMap.entries.firstOrNull { it.value == module.titleResId }?.key ?: ""
                    if (id !in hiddenList) orderedModules.add(module)
                }

                orderedModules
            }
        }
}
