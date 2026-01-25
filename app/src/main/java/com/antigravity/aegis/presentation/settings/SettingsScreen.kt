package com.antigravity.aegis.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.data.model.UserRole
import com.antigravity.aegis.data.security.BiometricPromptManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val biometricPromptState by viewModel.biometricPromptState.collectAsState()
    val context = LocalContext.current
    
    // BiometricPromptManager
    val activity = context as? androidx.fragment.app.FragmentActivity
    val biometricPromptManager = remember(activity) {
        if (activity != null) BiometricPromptManager(activity) else null
    }
    
    // Observe biometric prompt trigger
    LaunchedEffect(biometricPromptState) {
        biometricPromptState?.let { config ->
            biometricPromptManager?.showBiometricPrompt(
                title = context.getString(config.titleResId),
                description = context.getString(config.descriptionResId),
                cryptoObject = config.cryptoObject
            )
            viewModel.onBiometricPromptShown()
        }
    }
    
    // Observe biometric results
    LaunchedEffect(biometricPromptManager) {
        biometricPromptManager?.promptResults?.collect { result ->
            viewModel.onBiometricResult(result)
        }
    }

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
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_content_desc))
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
            
            // Handle UI State Feedback
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(uiState) {
                val state = uiState
                when (state) {
                    is SettingsUiState.Success -> {
                        android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                    is SettingsUiState.Error -> {
                        android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                    }
                    is SettingsUiState.LoggedOut -> {
                        android.widget.Toast.makeText(context, "Sesión expirada. Identifícate de nuevo.", android.widget.Toast.LENGTH_LONG).show()
                        onLogout()
                    }
                    else -> Unit
                }
            }

            // Loading Indicator
            if (uiState is SettingsUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text((uiState as SettingsUiState.Loading).message)
            }

            // General / Appearance Section
            val config = viewModel.userConfig.collectAsState().value
            
            SettingsSection(title = stringResource(R.string.general_section_title)) {
                // Language Selector
                Text("Idioma / Language", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config?.language == "es",
                        onClick = { viewModel.updateLanguage("es") },
                        label = { Text("Español") }
                    )
                    FilterChip(
                        selected = config?.language == "en",
                        onClick = { viewModel.updateLanguage("en") },
                        label = { Text("English") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Theme Selector
                Text("Tema / Theme", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     FilterChip(
                        selected = config?.themeMode == "system",
                        onClick = { viewModel.updateThemeMode("system") },
                        label = { Text("Sistema") }
                    )
                    FilterChip(
                        selected = config?.themeMode == "light",
                        onClick = { viewModel.updateThemeMode("light") },
                        label = { Text("Claro") }
                    )
                    FilterChip(
                        selected = config?.themeMode == "dark",
                        onClick = { viewModel.updateThemeMode("dark") },
                        label = { Text("Oscuro") }
                    )
                }
            }

            // Security Section
            SettingsSection(title = stringResource(R.string.security_section_title)) {
                if (isBiometricEnabled) {
                    Text(
                        text = stringResource(R.string.biometric_already_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Button(
                        onClick = { viewModel.enableBiometric() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.enable_biometrics_button))
                    }
                }
            }

            // Database Section
            SettingsSection(title = stringResource(R.string.database_section_title)) {
                Button(
                    onClick = { exportLauncher.launch("aegis_backup.db") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.export_database))
                }
                
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/x-sqlite3", "*/*")) }, // Mime type might vary
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.import_database_overwrite))
                }
            }

            // User Management Section
            SettingsSection(title = stringResource(R.string.user_management_section_title)) {
                Button(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.create_new_user))
                }
            }

            // Session Section
            SettingsSection(title = stringResource(R.string.session_section_title)) {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.logout))
                }
            }
        }
    }
    
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, pin, role ->
                viewModel.createNewUser(name = name, language = "es", pin = pin, role = role)
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
        title = { Text(stringResource(R.string.new_user)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(stringResource(R.string.name_label)) }
                )
                OutlinedTextField(
                    value = pin, 
                    onValueChange = { if (it.length <= 6) pin = it }, 
                    label = { Text(stringResource(R.string.pin_digits_label)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, pin, UserRole.USER) }, // Default to User for now
                enabled = name.isNotBlank() && pin.length >= 4
            ) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}
