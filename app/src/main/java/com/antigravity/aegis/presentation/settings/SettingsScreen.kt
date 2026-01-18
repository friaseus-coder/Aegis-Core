package com.antigravity.aegis.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri ->
        uri?.let { viewModel.exportDatabase(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDatabase(it) }
    }

    // Effect for UI State
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.LoggedOut -> onLogout()
            is SettingsUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.clearState()
            }
            is SettingsUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearState()
            }
            else -> {}
        }
    }
    
    // Add User Dialog State
    var showAddUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Loading Indicator
            if (uiState is SettingsUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text((uiState as SettingsUiState.Loading).message)
            }

            // Database Section
            SettingsSection(title = "Base de Datos") {
                Button(
                    onClick = { exportLauncher.launch("aegis_backup.db") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exportar Base de Datos")
                }
                
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/x-sqlite3", "*/*")) }, // Mime type might vary
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Importar Base de Datos (Sobreescribir)")
                }
            }

            // User Management Section
            SettingsSection(title = "Gestión de Usuarios") {
                Button(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Nuevo Usuario")
                }
            }

            // Session Section
            SettingsSection(title = "Sesión") {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
    
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, pin, role ->
                viewModel.createNewUser(name, pin, role)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String, String, UserRole) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = pin, 
                    onValueChange = { if (it.length <= 6) pin = it }, 
                    label = { Text("PIN (4-6 dígitos)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, pin, UserRole.USER) }, // Default to User for now
                enabled = name.isNotBlank() && pin.length >= 4
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
