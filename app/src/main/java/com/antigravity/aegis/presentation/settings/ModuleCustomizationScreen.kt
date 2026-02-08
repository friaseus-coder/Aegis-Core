package com.antigravity.aegis.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                    moduleConfigs.forEach { module ->
                        ModuleConfigItem(
                            moduleName = stringResource(module.nameResId),
                            isVisible = module.isVisible,
                            onVisibilityChange = { isVisible ->
                                viewModel.updateModuleVisibility(module.id, isVisible)
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

@Composable
fun ModuleConfigItem(
    moduleName: String,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = moduleName,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Switch(
            checked = isVisible,
            onCheckedChange = onVisibilityChange
        )
    }
}
