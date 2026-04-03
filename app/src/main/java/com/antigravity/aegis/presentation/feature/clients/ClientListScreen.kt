package com.antigravity.aegis.presentation.feature.clients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.presentation.crm.CrmViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import com.antigravity.aegis.presentation.common.ImportConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    viewModel: ClientListViewModel = hiltViewModel(),
    crmViewModel: CrmViewModel,
    onNavigateToClientDetail: (Int) -> Unit,
    onNavigateToClientCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transferState by crmViewModel.transferState.collectAsState()
    val context = LocalContext.current

    var showImportMenu by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }
    var isReplaceMode by remember { mutableStateOf(false) }

    // Import Picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            crmViewModel.validateImport(uri, isReplaceMode)
        }
    }

    // Export Picker
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            crmViewModel.exportClientsToUri(uri)
        }
    }

    // Handling States
    when (val state = transferState) {
        is CrmViewModel.TransferState.Success -> {
            val msg = if (state.resId != null) stringResource(state.resId, state.arg ?: "") else state.message ?: ""
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            crmViewModel.resetTransferState()
        }
        is CrmViewModel.TransferState.Error -> {
            val msg = if (state.resId != null) stringResource(state.resId, state.arg ?: "") else state.message ?: ""
            Toast.makeText(context, stringResource(R.string.general_error_prefix, msg), Toast.LENGTH_LONG).show()
             crmViewModel.resetTransferState()
        }
        is CrmViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { crmViewModel.resetTransferState() },
                title = { Text(stringResource(R.string.data_import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { crmViewModel.resetTransferState() }) { Text(stringResource(R.string.general_ok)) } }
            )
        }
        is CrmViewModel.TransferState.ValidationSuccessReplace -> {
            com.antigravity.aegis.presentation.crm.ClientImportSummaryDialog(
                isReplace = true,
                validCount = 0,
                onConfirm = { crmViewModel.confirmImport(state.uri, true) },
                onDismiss = { crmViewModel.resetTransferState() }
            )
        }
        is CrmViewModel.TransferState.ValidationSuccessAppend -> {
            com.antigravity.aegis.presentation.crm.ClientImportSummaryDialog(
                isReplace = false,
                validCount = state.validCount,
                onConfirm = { crmViewModel.confirmImport(state.uri, false) },
                onDismiss = { crmViewModel.resetTransferState() }
            )
        }
        is CrmViewModel.TransferState.Loading -> {
             // Show simple loading if needed, or rely on toast
        }
        else -> {}
    }

    if (showImportMenu) {
        com.antigravity.aegis.presentation.crm.ClientImportDialog(
            onDismiss = { showImportMenu = false },
            onDownloadTemplate = { crmViewModel.downloadTemplate() },
            onUploadReplace = { 
                isReplaceMode = true
                importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) 
            },
            onUploadAppend = {
                isReplaceMode = false
                importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))
            }
        )
    }

    if (showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            title = { Text(stringResource(R.string.crm_export_confirm_title)) },
            text = { Text(stringResource(R.string.crm_export_confirm_text)) },
            confirmButton = {
                Button(onClick = {
                    showExportConfirmDialog = false
                    exportLauncher.launch("clients_export_${System.currentTimeMillis()}.csv")
                }) {
                    Text(stringResource(R.string.general_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirmDialog = false }) {
                    Text(stringResource(R.string.general_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                title = stringResource(R.string.crm_clients_title), // Changed title to be correct
                actions = {
                    IconButton(onClick = { showExportConfirmDialog = true }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.data_export_csv))
                    }
                    IconButton(onClick = { showImportMenu = true }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.data_import_csv))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToClientCreate) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.crm_clients_add_button))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text(stringResource(R.string.action_search_hint_min_3)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                maxLines = 1,
                trailingIcon = {
                     if (uiState.searchQuery.isNotEmpty()) {
                         IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                             Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_search))
                         }
                     } else {
                         Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                     }
                }
            )

            // Filters Row
            Row(
                 modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                 FilterChip(
                     selected = uiState.filterType == null,
                     onClick = { viewModel.onFilterSelected(null) },
                     label = { Text(stringResource(R.string.filter_all)) }
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(
                     selected = uiState.filterType == ClientType.PARTICULAR, 
                     onClick = { 
                         viewModel.onFilterSelected(if (uiState.filterType == ClientType.PARTICULAR) null else ClientType.PARTICULAR) 
                     }, 
                     label = { Text(stringResource(R.string.filter_individuals)) }
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(
                     selected = uiState.filterType == ClientType.EMPRESA, 
                     onClick = { 
                         viewModel.onFilterSelected(if (uiState.filterType == ClientType.EMPRESA) null else ClientType.EMPRESA) 
                     }, 
                     label = { Text(stringResource(R.string.filter_companies)) }
                 )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(uiState.clients) { client ->
                        ListItem(
                            headlineContent = { 
                                Text(
                                    if (client.tipoCliente == ClientType.PARTICULAR) "${client.firstName} ${client.lastName}" 
                                    else client.firstName
                                ) 
                            },
                            supportingContent = { 
                                Column {
                                    val detalle = if (client.tipoCliente == ClientType.EMPRESA) client.razonSocial else client.nifCif
                                    val typeStr = when (client.tipoCliente) {
                                        ClientType.PARTICULAR -> stringResource(R.string.client_type_particular)
                                        ClientType.EMPRESA -> stringResource(R.string.client_type_empresa)
                                    }
                                    val catStr = when (client.categoria) {
                                        com.antigravity.aegis.domain.model.ClientCategory.POTENTIAL -> stringResource(R.string.client_cat_potential)
                                        com.antigravity.aegis.domain.model.ClientCategory.ACTIVE -> stringResource(R.string.client_cat_active)
                                        com.antigravity.aegis.domain.model.ClientCategory.INACTIVE -> stringResource(R.string.client_cat_inactive)
                                    }

                                    Text(
                                        text = if (!detalle.isNullOrEmpty()) "$typeStr • $detalle" else typeStr,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = catStr,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier.clickable { 
                                 onNavigateToClientDetail(client.id) 
                            }
                        )
                        Divider()
                    }
                    if (uiState.clients.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.crm_clients_empty))
                            }
                        }
                    }
                }
            }
        }
    }
}
