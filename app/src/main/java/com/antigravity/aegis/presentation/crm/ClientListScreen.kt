package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.presentation.components.BovedaLogo
import com.antigravity.aegis.ui.theme.LocalCompanyLogoUri
import androidx.compose.ui.Alignment
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    viewModel: CrmViewModel,
    onNavigateToClientDetail: (Int) -> Unit,
    onNavigateToClientCreate: () -> Unit
) {
    val clients by viewModel.allClients.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    
    val context = LocalContext.current
    
    // Filters State
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<String?>(null) }
    var showImportMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(searchQuery) { viewModel.setSearchQuery(searchQuery) }
    LaunchedEffect(filterType) { viewModel.setFilterType(filterType) }
    
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
            val message = if (state.resId != null) {
                if (state.arg != null) stringResource(state.resId, state.arg)
                else stringResource(state.resId)
            } else state.message ?: ""
            
            LaunchedEffect(state) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.resetTransferState()
            }
        }
        is CrmViewModel.TransferState.Error -> {
            val message = if (state.resId != null) stringResource(state.resId) else state.message ?: "Error"
            LaunchedEffect(state) {
                Toast.makeText(context, context.getString(R.string.general_error_prefix, message), Toast.LENGTH_LONG).show()
                viewModel.resetTransferState()
            }
        }
        is CrmViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text(stringResource(R.string.data_import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text(stringResource(R.string.general_ok)) } }
            )
        }
        is CrmViewModel.TransferState.ValidationSuccess -> {
            ClientImportConfirmDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onDismiss = { viewModel.resetTransferState() }
            )
        }
        is CrmViewModel.TransferState.Loading -> {
             // Optional: LinearProgressIndicator() or similar
        }
        else -> {}
    }

    if (showImportMenu) {
        ClientImportDialog(
            onDismiss = { showImportMenu = false },
            onDownloadTemplate = { viewModel.downloadTemplate() },
            onUploadFile = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }
        )
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = { viewModel.exportClients() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.crm_clients_mass_export_btn))
                    }
                    IconButton(onClick = { showImportMenu = true }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.crm_clients_mass_import_btn))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToClientCreate() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.crm_clients_add_button))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Screen Title in Body
            Text(
                text = stringResource(R.string.crm_clients_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.ui_search_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                maxLines = 1,
                trailingIcon = {
                     if (searchQuery.isNotEmpty()) {
                         IconButton(onClick = { searchQuery = "" }) {
                             Icon(Icons.Default.Clear, stringResource(R.string.ui_clear_label))
                         }
                     } else {
                         Icon(Icons.Default.Search, stringResource(R.string.ui_search_hint))
                     }
                }
            )

            // Filters Row
            Row(
                 modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                 FilterChip(
                     selected = filterType == null,
                     onClick = { filterType = null },
                     label = { Text(stringResource(R.string.ui_filter_all)) }
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(selected = filterType == "Particular", onClick = { filterType = "Particular" }, label = { Text(stringResource(R.string.ui_filter_individuals)) })
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(selected = filterType == "Empresa", onClick = { filterType = "Empresa" }, label = { Text(stringResource(R.string.ui_filter_companies)) })
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(clients, key = { it.id }) { client ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                if (client.tipoCliente == ClientType.PARTICULAR) "${client.firstName} ${client.lastName}" 
                                else client.firstName // "Nombre Comercial"
                            ) 
                        },
                        supportingContent = { 
                            Column {
                                val detalle = if (client.tipoCliente == ClientType.EMPRESA) client.razonSocial else client.nifCif
                                Text(
                                    text = if (!detalle.isNullOrEmpty()) "${client.tipoCliente.name} • $detalle" else client.tipoCliente.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = client.categoria.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (client.categoria.name == "ACTIVE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }

                        },
                        modifier = Modifier.clickable { 
                             onNavigateToClientDetail(client.id) 
                        }
                    )
                    HorizontalDivider()
                }
                if (clients.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(if (searchQuery.length in 1..2) stringResource(R.string.ui_search_min_chars) else stringResource(R.string.crm_clients_empty))
                        }
                    }
                }
            }
        }
    }
}
