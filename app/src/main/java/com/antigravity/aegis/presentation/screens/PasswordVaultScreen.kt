package com.antigravity.aegis.presentation.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.PasswordEntity
import com.antigravity.aegis.data.security.BiometricPromptManager
import com.antigravity.aegis.data.security.BiometricResult
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.presentation.vault.PasswordVaultViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVaultScreen(
    viewModel: PasswordVaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val passwords by viewModel.allPasswords.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val decryptedPasswords by viewModel.decryptedPasswords.collectAsState()
    
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var showAddDialog by remember { mutableStateOf(false) }
    
    val promptManager = remember(activity) {
        activity?.let { BiometricPromptManager(it) }
    }

    LaunchedEffect(promptManager) {
        promptManager?.promptResults?.collectLatest { result ->
            when (result) {
                is BiometricResult.AuthenticationSuccess -> {
                    viewModel.unlockVault()
                }
                is BiometricResult.AuthenticationError -> {
                    Toast.makeText(context, context.getString(R.string.general_error_prefix, result.error), Toast.LENGTH_SHORT).show()
                }
                BiometricResult.AuthenticationFailed -> {
                    Toast.makeText(context, context.getString(R.string.vault_auth_failed), Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.ui_back))
                    }
                },
                actions = {
                    if (isUnlocked) {
                        IconButton(onClick = { viewModel.lockVault() }) {
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.vault_lock_button))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isUnlocked) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vault_add_password))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isUnlocked) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.vault_locked_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        promptManager?.showBiometricPrompt(
                            title = context.getString(R.string.vault_biometric_prompt_title),
                            description = context.getString(R.string.vault_biometric_prompt_desc)
                        )
                    }) {
                        Text(stringResource(R.string.vault_unlock_biometric_button))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(passwords, key = { it.id }) { password ->
                        PasswordItem(
                            password = password,
                            decryptedValue = decryptedPasswords[password.id],
                            onDecrypt = { viewModel.decryptPassword(password) },
                            onDelete = { viewModel.deletePassword(password) }
                        )
                    }
                    if (passwords.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.vault_empty_state),
                                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPasswordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, user, pass, web, note ->
                viewModel.addPassword(title, user, pass, web, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PasswordItem(
    password: PasswordEntity,
    decryptedValue: String?,
    onDecrypt: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = password.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = password.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    IconButton(onClick = onDecrypt) {
                        Icon(
                            imageVector = if (decryptedValue != null) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(R.string.vault_visibility_desc)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.general_delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            if (decryptedValue != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = decryptedValue,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            } else {
                Text(text = "••••••••", modifier = Modifier.padding(vertical = 8.dp))
            }

            if (!password.website.isNullOrBlank()) {
                Text(text = password.website, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.vault_delete_title)) },
            text = { Text(stringResource(R.string.vault_delete_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.general_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.general_cancel))
                }
            }
        )
    }
}

@Composable
fun AddPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vault_new_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.vault_label_title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.vault_label_user)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.vault_label_pass)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text(stringResource(R.string.vault_label_website)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.vault_label_notes)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, username, password, website.ifBlank { null }, notes.ifBlank { null }) },
                enabled = title.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(R.string.general_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}
