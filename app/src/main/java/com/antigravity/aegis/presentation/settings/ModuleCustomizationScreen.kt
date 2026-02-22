package com.antigravity.aegis.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R

/**
 * Pantalla para personalizar la visibilidad y orden de los módulos del dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleCustomizationScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val moduleConfigs by viewModel.getModuleConfigurations()
        .collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.module_customization_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Descripción
            Text(
                text = stringResource(R.string.module_customization_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Lista de módulos con toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    moduleConfigs.forEachIndexed { index, module ->
                        ModuleConfigItem(
                            moduleName = stringResource(module.nameResId),
                            isVisible = module.isVisible,
                            isFirst = index == 0,
                            isLast = index == moduleConfigs.size - 1,
                            onVisibilityChange = { isVisible ->
                                viewModel.updateModuleVisibility(module.id, isVisible)
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val newList = moduleConfigs.map { it.id }.toMutableList()
                                    newList.swap(index, index - 1)
                                    viewModel.updateModuleOrder(newList)
                                }
                            },
                            onMoveDown = {
                                if (index < moduleConfigs.size - 1) {
                                    val newList = moduleConfigs.map { it.id }.toMutableList()
                                    newList.swap(index, index + 1)
                                    viewModel.updateModuleOrder(newList)
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón restaurar valores predeterminados
            OutlinedButton(
                onClick = { viewModel.restoreDefaultModules() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.module_customization_restore_button))
            }
        }
    }
}

// Helper extension function for swapping elements
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

@Composable
fun ModuleConfigItem(
    moduleName: String,
    isVisible: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name
        Text(
            text = moduleName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        // Reordering arrows
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up"
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down"
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Visibility toggle
        Switch(
            checked = isVisible,
            onCheckedChange = onVisibilityChange
        )
    }
}
