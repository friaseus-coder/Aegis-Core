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
import com.antigravity.aegis.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToModuleCustomization: () -> Unit,
    onNavigateToTemplates: () -> Unit
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
    

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

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
                        selected = config?.language == "ca",
                        onClick = { viewModel.updateLanguage("ca") },
                        label = { Text(stringResource(R.string.language_catalan)) }
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Currency Selector
                val currencies = listOf(
                    "USD" to stringResource(R.string.currency_usd),
                    "EUR" to stringResource(R.string.currency_eur),
                    "JPY" to stringResource(R.string.currency_jpy),
                    "GBP" to stringResource(R.string.currency_gbp),
                    "CNY" to stringResource(R.string.currency_cny),
                    "AUD" to stringResource(R.string.currency_aud),
                    "OTHER" to stringResource(R.string.currency_other)
                )

                Text(stringResource(R.string.settings_currency_title), style = MaterialTheme.typography.bodyMedium)
                var currencyExpanded by remember { mutableStateOf(false) }
                val selectedCurrencyLabel = currencies.firstOrNull { it.first == (config?.currency ?: "EUR") }?.second
                    ?: stringResource(R.string.currency_eur)

                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = !currencyExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCurrencyLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_currency_title)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        currencies.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateCurrency(code)
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
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

            // Templates Section
            SettingsSection(title = "Gestión de Plantillas") {
                Button(
                    onClick = onNavigateToTemplates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gestionar Plantillas de Proyecto")
                }

            }

            // Legal & Privacy Section
            SettingsSection(title = stringResource(R.string.settings_section_legal)) {
                Button(
                    onClick = { showPrivacyPolicyDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                ) {
                    Text(stringResource(R.string.settings_privacy_policy_button))
                }
            }
        }
    }
    


    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text(stringResource(R.string.privacy_policy_title)) },
            text = { 
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.privacy_policy_content))
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
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

