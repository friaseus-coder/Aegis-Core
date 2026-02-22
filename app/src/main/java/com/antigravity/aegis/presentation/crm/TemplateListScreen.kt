package com.antigravity.aegis.presentation.crm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import java.text.SimpleDateFormat
import java.util.Date

import java.util.Locale
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.material.icons.filled.FileOpen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    viewModel: CrmViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val context = LocalContext.current

    // File Pickers
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importTemplate(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        // We need to know WHICH template to export.
        // This launcher needs to be triggered after selection.
        // Compose constraints: Launchers must be declared at top level.
        // We can use a side effect or just store the pending URI action.
        // Better: When "Export" is clicked on item, we set a state "pendingExportTemplateId".
        // Then we launch. On result, we use that ID.
    }
    
    // State to hold valid export request
    var pendingExportTemplateId by remember { mutableStateOf<Int?>(null) }
    
    val exportLauncherWithId = rememberLauncherForActivityResult(
         contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { 
             pendingExportTemplateId?.let { id -> viewModel.exportTemplate(id, it) }
        }
        pendingExportTemplateId = null
    }

    // Filter State
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // Extract categories
    val categories = remember(templates) {
        templates.mapNotNull { it.category }.distinct().sorted()
    }
    
    // Filter templates
    val filteredTemplates = remember(templates, selectedCategory) {
        if (selectedCategory == null) {
            templates
        } else {
            templates.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                title = stringResource(R.string.crm_templates_title),
                actions = {
                    IconButton(onClick = { viewModel.shareSampleTemplate() }) {
                        Icon(
                            Icons.Default.FileOpen,
                            contentDescription = stringResource(R.string.crm_templates_download_sample)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.crm_templates_import))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Transfer Feedback
            if (transferState is CrmViewModel.TransferState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (transferState is CrmViewModel.TransferState.Success) {
                val state = transferState as CrmViewModel.TransferState.Success
                val msg = if (state.resId != null) stringResource(state.resId, state.arg ?: "") else state.message ?: ""
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (transferState is CrmViewModel.TransferState.Error) {
                val state = transferState as CrmViewModel.TransferState.Error
                val msg = if (state.resId != null) stringResource(state.resId, state.arg ?: "") else state.message ?: ""
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                 // Categories Header
                if (categories.isNotEmpty()) {
                    item {
                        ScrollableTabRow(
                            selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1,
                            edgePadding = 16.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                // Optional custom indicator
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1])
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                text = { Text(stringResource(R.string.filter_all)) }
                            )
                            categories.forEach { category ->
                                Tab(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    text = { Text(category) }
                                )
                            }
                        }
                    }
                }

                items(filteredTemplates) { template ->
                    ListItem(
                        headlineContent = { Text(template.name) },
                        supportingContent = { Text(stringResource(R.string.crm_templates_created, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(template.startDate)))) },
                        trailingContent = {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.crm_templates_view_edit)) },
                                        onClick = { 
                                            expanded = false
                                            onNavigateToDetail(template.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.crm_templates_export_json)) },
                                        onClick = {
                                            expanded = false
                                            pendingExportTemplateId = template.id
                                            exportLauncherWithId.launch("template_${template.name.replace(" ", "_")}.json")
                                        }
                                    )
                                    // Add "Delete" here if needed
                                }
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToDetail(template.id) }
                    )
                    Divider()
                }
                
                if (filteredTemplates.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(stringResource(R.string.crm_templates_empty))
                        }
                    }
                }
            }
        }
    }
}
