package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    viewModel: CrmViewModel,
    onNavigateToClientDetail: (Int) -> Unit
) {
    val clients by viewModel.allClients.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Import Picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.validateImport(uri)
        }
    }

    // Handling States
    when (val state = transferState) {
        is CrmViewModel.TransferState.Success -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is CrmViewModel.TransferState.Error -> {
            Toast.makeText(context, context.getString(R.string.import_error_prefix, state.message), Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is CrmViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text(stringResource(R.string.import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text(stringResource(R.string.ok_button)) } }
            )
        }
        is CrmViewModel.TransferState.ValidationSuccess -> {
            ImportConfirmationDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onCancel = { viewModel.resetTransferState() }
            )
        }
        is CrmViewModel.TransferState.Loading -> {
             // Show simple loading, usually automatic via box, but here blocking interaction
             // For MVP, just a toast or background indicator
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clients_title)) },
                actions = {
                    IconButton(onClick = { viewModel.exportClients() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.export_csv))
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.import_csv))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_client_fab))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(clients) { client ->
                ListItem(
                    headlineContent = { Text(client.name) },
                    supportingContent = { Text(client.email ?: stringResource(R.string.client_email_placeholder)) },
                    modifier = Modifier.clickable { onNavigateToClientDetail(client.id) }
                )
                Divider()
            }
            if (clients.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(stringResource(R.string.no_clients))
                    }
                }
            }
        }

        if (showAddDialog) {
            AddClientDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, email, phone, notes ->
                    viewModel.createClient(name, email, phone, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddClientDialog(onDismiss: () -> Unit, onConfirm: (String, String?, String?, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_client_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email_label)) })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone_label)) })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.notes_label)) })
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) onConfirm(name, email.ifBlank { null }, phone.ifBlank { null }, notes.ifBlank { null }) 
            }) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}
