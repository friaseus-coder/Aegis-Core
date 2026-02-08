package com.antigravity.aegis.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.UserRole
import com.antigravity.aegis.data.security.BiometricPromptManager
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToModuleCustomization: () -> Unit
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
                Toast.makeText(context, state.message.asString(context), Toast.LENGTH_SHORT).show()
                viewModel.clearState()
            }
            is SettingsUiState.Error -> {
                Toast.makeText(context, state.message.asString(context), Toast.LENGTH_LONG).show()
                viewModel.clearState()
            }
            else -> {}
        }
    }
    
    // Add User Dialog State
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.UserEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Handle UI State Feedback (Redundant? Lines 82-93 also handle it?)
            // The previous block (80-93) uses `context` from `LocalContext.current` at line 39.
            // This block (122-137) used a new `context`.
            // I will remove this duplicate block and just keep the Loading indicator logic which is unique here, or better yet, verify logic.
            // Lines 80-93 handle the Toast.
            // Lines 122-137 seem to be a duplicate added during previous edits or original code? 
            // Ah, I see in original file lines 80-93 and 122-137. I should probably clean it up.
            // But for now, let's just make sure it compiles.
            // The `uiState` collection in 122 seems to shadow/copy.
            
            // Actually, let's just update the duplicate block inside Column if I can't remove it safely without viewing.
            // Wait, looking at file content:
            // Line 39: val context = LocalContext.current
            // Line 80: LaunchedEffect(uiState) ... Toast ...
            // Line 121: val context = androidx.compose.ui.platform.LocalContext.current
            // Line 122: LaunchedEffect... 
            // It IS duplicated. I should remove the second one.
            
            // Loading Indicator
            if (uiState is SettingsUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text((uiState as SettingsUiState.Loading).message.asString(context))
            }

            // General / Appearance Section
            val config = viewModel.userConfig.collectAsState().value
            
            SettingsSection(title = stringResource(R.string.settings_section_general)) {
                // Language Selector
                Text(stringResource(R.string.general_language), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config?.language == "es",
                        onClick = { viewModel.updateLanguage("es") },
                        label = { Text(stringResource(R.string.language_spanish)) }
                    )
                    FilterChip(
                        selected = config?.language == "en",
                        onClick = { viewModel.updateLanguage("en") },
                        label = { Text(stringResource(R.string.language_english)) }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Theme Selector
                Text(stringResource(R.string.general_theme), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     FilterChip(
                        selected = config?.themeMode == "system",
                        onClick = { viewModel.updateThemeMode("system") },
                        label = { Text(stringResource(R.string.theme_system)) }
                    )
                    FilterChip(
                        selected = config?.themeMode == "light",
                        onClick = { viewModel.updateThemeMode("light") },
                        label = { Text(stringResource(R.string.theme_light)) }
                    )
                    FilterChip(
                        selected = config?.themeMode == "dark",
                        onClick = { viewModel.updateThemeMode("dark") },
                        label = { Text(stringResource(R.string.theme_dark)) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Module Customization Button
                Button(
                    onClick = onNavigateToModuleCustomization,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_customize_modules_button))
                }
            }

            // Security Section
            SettingsSection(title = stringResource(R.string.settings_section_security)) {
                if (isBiometricEnabled) {
                    Text(
                        text = stringResource(R.string.settings_biometric_status_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Button(
                        onClick = { viewModel.enableBiometric() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.auth_login_enable_biometrics_button))
                    }
                }
            }

            // Database Section
            SettingsSection(title = stringResource(R.string.settings_section_database)) {
                Button(
                    onClick = onNavigateToBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_backup_manage_button))
                }
                
                Button(
                    onClick = { viewModel.shareBackup(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.settings_share_backup_button))
                }
            }

            // User Management Section
            SettingsSection(title = stringResource(R.string.settings_section_users)) {
                Button(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_create_user_button))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val users by viewModel.users.collectAsState()
                
                if (users.isNotEmpty()) {
                    Text(stringResource(R.string.settings_users_list_title), style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        users.forEach { user ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { userToEdit = user }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(user.name, fontWeight = FontWeight.Bold)
                                        val roleText = when (user.role) {
                                            UserRole.ADMIN -> stringResource(R.string.settings_role_admin_desc)
                                            UserRole.MANAGER -> stringResource(R.string.settings_role_manager_title)
                                            UserRole.USER, UserRole.GUEST -> stringResource(R.string.settings_role_worker_title)
                                        }
                                        Text(
                                            text = roleText, 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (user.role == UserRole.ADMIN || user.role == UserRole.MANAGER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Company Details Section
            val configState = viewModel.userConfig.collectAsState()
            val currentConfig = configState.value
            
            SettingsSection(title = stringResource(R.string.settings_company_details_title)) {
                var companyName by remember(currentConfig) { mutableStateOf(currentConfig?.companyName ?: "") }
                var address by remember(currentConfig) { mutableStateOf(currentConfig?.companyAddress ?: "") }
                var postalCode by remember(currentConfig) { mutableStateOf(currentConfig?.companyPostalCode ?: "") }
                var city by remember(currentConfig) { mutableStateOf(currentConfig?.companyCity ?: "") }
                var province by remember(currentConfig) { mutableStateOf(currentConfig?.companyProvince ?: "") }
                var dniCif by remember(currentConfig) { mutableStateOf(currentConfig?.companyDniCif ?: "") }
                
                // Text Fields
                OutlinedTextField(
                    value = companyName, onValueChange = { companyName = it },
                    label = { Text(stringResource(R.string.settings_company_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dniCif, onValueChange = { dniCif = it },
                    label = { Text(stringResource(R.string.settings_company_dni_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text(stringResource(R.string.settings_company_address_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = postalCode, onValueChange = { postalCode = it },
                        label = { Text(stringResource(R.string.settings_company_postal_code_label)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = city, onValueChange = { city = it },
                        label = { Text(stringResource(R.string.settings_company_city_label)) },
                        modifier = Modifier.weight(2f)
                    )
                }
                OutlinedTextField(
                    value = province, onValueChange = { province = it },
                    label = { Text(stringResource(R.string.settings_company_province_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = {
                        viewModel.updateCompanyConfig(companyName, address, postalCode, city, province, dniCif)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.settings_company_save_button))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Logo Section
                Text(stringResource(R.string.settings_logo_title), style = MaterialTheme.typography.titleSmall)
                
                val logoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    uri?.let { viewModel.updateCompanyLogo(it) }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview
                    if (currentConfig?.companyLogoUri != null) {
                        coil.compose.AsyncImage(
                            model = currentConfig.companyLogoUri,
                            contentDescription = "Logo Empresa",
                            modifier = Modifier.size(80.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.settings_logo_none), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Button(
                        onClick = { 
                            logoLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Text(stringResource(R.string.settings_logo_select_button))
                    }
                    
                    if (currentConfig?.companyLogoUri != null) {
                         TextButton(
                            onClick = { viewModel.deleteCompanyLogo() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.settings_logo_delete_button))
                        }
                    }
                }
            }

            // Session Section
            SettingsSection(title = stringResource(R.string.settings_section_session)) {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.settings_logout_button))
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
    
    userToEdit?.let { user ->
        EditRoleDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onConfirm = { newRole ->
                viewModel.updateUserRole(user.id, newRole)
                userToEdit = null
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
    
    var isEmpresa by remember { mutableStateOf(false) }
    var isParticular by remember { mutableStateOf(true) } // Default to Worker
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_create_user_button)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(stringResource(R.string.general_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pin, 
                    onValueChange = { if (it.length <= 6) pin = it }, 
                    label = { Text(stringResource(R.string.auth_login_pin_label)) },
                    singleLine = true
                )
                
                // Role Selection
                Column {
                    Text(stringResource(R.string.settings_profile_active_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isEmpresa,
                            onCheckedChange = { isEmpresa = it }
                        )
                        Column {
                            Text(stringResource(R.string.settings_role_manager_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.settings_role_manager_desc), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isParticular,
                            onCheckedChange = { isParticular = it }
                        )
                         Column {
                            Text(stringResource(R.string.settings_role_worker_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.settings_role_worker_desc), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isValid = name.isNotBlank() && pin.length >= 4 && (isEmpresa || isParticular)
            TextButton(
                onClick = { 
                    val role = when {
                        isEmpresa && isParticular -> UserRole.ADMIN
                        isEmpresa -> UserRole.MANAGER
                        else -> UserRole.USER
                    }
                    onConfirm(name, pin, role) 
                }, 
                enabled = isValid
            ) {
                Text(stringResource(R.string.general_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@Composable
fun EditRoleDialog(
    user: com.antigravity.aegis.data.local.entity.UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (UserRole) -> Unit
) {
     var isEmpresa by remember { mutableStateOf(user.role == UserRole.MANAGER || user.role == UserRole.ADMIN) }
     var isParticular by remember { mutableStateOf(user.role == UserRole.USER || user.role == UserRole.ADMIN) }
     
     AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_role_edit_prefix, user.name)) },
        text = {
             Column {
                Text(stringResource(R.string.settings_profile_active_label), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isEmpresa,
                        onCheckedChange = { isEmpresa = it }
                    )
                    Column {
                        Text(stringResource(R.string.settings_role_manager_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isParticular,
                        onCheckedChange = { isParticular = it }
                    )
                     Column {
                        Text(stringResource(R.string.settings_role_worker_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            val isValid = isEmpresa || isParticular
            TextButton(
                onClick = { 
                    val newRole = when {
                        isEmpresa && isParticular -> UserRole.ADMIN
                        isEmpresa -> UserRole.MANAGER
                        else -> UserRole.USER
                    }
                    onConfirm(newRole)
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.general_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
     )
}
