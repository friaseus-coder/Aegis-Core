package com.antigravity.aegis.presentation.crm

import com.antigravity.aegis.domain.model.CrmStatus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.presentation.components.SyncBanner
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CrmViewModel,
    onNavigateToClients: () -> Unit,
    onNavigateToKanban: () -> Unit,
    onNavigateToProject: (Int) -> Unit
) {
    val activeProjects by viewModel.activeRootProjects.collectAsState()
    val allClients by viewModel.allClients.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val categories by viewModel.templateCategories.collectAsState()
    val selectedStatuses by viewModel.selectedProjectStatuses.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncingCalendar.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.crm_dashboard_new_project))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Screen Title
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToKanban,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.crm_kanban_title))
                }
                OutlinedButton(
                    onClick = onNavigateToClients,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.dashboard_manage_clients))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.dashboard_active_projects),
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // -- Status Filter --
            val allStatuses = listOf(CrmStatus.DRAFT, CrmStatus.SENT, CrmStatus.ACTIVE, CrmStatus.WON, CrmStatus.LOST, CrmStatus.ARCHIVED, CrmStatus.CLOSED)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(allStatuses) { status ->
                    FilterChip(
                        selected = selectedStatuses.contains(status),
                        onClick = { viewModel.toggleProjectStatusFilter(status) },
                        label = { Text(status) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeProjects) { project ->
                    Card(
                        onClick = { onNavigateToProject(project.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(project.name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.dashboard_status_label, project.status), style = MaterialTheme.typography.bodyMedium)
                                project.endDate?.let {
                                    Text(stringResource(R.string.dashboard_deadline_label, it), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { projectToDelete = project }) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar proyecto", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                
                if (activeProjects.isEmpty()) {
                    item {
                        Text(stringResource(R.string.dashboard_no_projects), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            SyncBanner(
                pendingCount = pendingSyncCount,
                isSyncing = isSyncing,
                onSyncNow = { viewModel.syncToCalendar() }
            )
        }
    }

        if (showCreateDialog) {
            CreateProjectWizard(
                clients = allClients,
                categories = categories,
                templates = templates,
                onDismiss = { showCreateDialog = false },
                onConfirm = { clientId, name, startDate, endDate, templateId, category ->
                    if (templateId != null) {
                         viewModel.createProjectFromTemplate(templateId, clientId, name, startDate, endDate)
                    } else {
                         viewModel.createProject(clientId, name, com.antigravity.aegis.domain.model.CrmStatus.ACTIVE, startDate, endDate, category)
                    }
                    showCreateDialog = false
                },
                onCreateClient = onNavigateToClients
            )
        }

        if (projectToDelete != null) {
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Borrar proyecto") },
                text = {
                    Text("Se borrará el proyecto \"${projectToDelete!!.name}\" y todos sus presupuestos asociados en el CRM. Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProjectWithQuotes(
                                projectId = projectToDelete!!.id,
                                onSuccess = {
                                    projectToDelete = null
                                },
                                onError = { errorMsg ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Error al borrar: $errorMsg",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    projectToDelete = null
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Borrar", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

@Composable
fun CreateProjectWizard(
    clients: List<Client>,
    categories: List<String>,
    templates: List<com.antigravity.aegis.data.local.entity.ProjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Long, Long?, Int?, String?) -> Unit,
    onCreateClient: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTemplate by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    var projectName by remember { mutableStateOf("") }
    
    // Date pickers logic would go here, omitting for brevity, using current/null
    val startDate = System.currentTimeMillis()
    val endDate: Long? = null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = when(step) {
                        1 -> stringResource(R.string.crm_wizard_select_client)
                        2 -> stringResource(R.string.crm_wizard_select_category)
                        3 -> stringResource(R.string.crm_wizard_select_template)
                        4 -> stringResource(R.string.crm_wizard_project_details)
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                when(step) {
                    1 -> {
                        // Client Search/Select
                         if (clients.isEmpty()) {
                             Text(stringResource(R.string.crm_wizard_no_clients))
                             Button(onClick = onCreateClient) { Text(stringResource(R.string.crm_wizard_create_client)) }
                         } else {
                             LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                 items(clients) { client ->
                                     ListItem(
                                         headlineContent = { 
                                             val name = if (client.tipoCliente == ClientType.PARTICULAR) "${client.firstName} ${client.lastName}" else client.firstName
                                             Text(name) 
                                         },
                                         modifier = Modifier.clickable {
                                             selectedClient = client
                                             step = 2
                                         }
                                     )

                                     HorizontalDivider()
                                 }
                             }
                         }
                    }
                    2 -> {
                        // Category Select
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.crm_wizard_no_category)) },
                                    modifier = Modifier.clickable {
                                        selectedCategory = null
                                        step = 3
                                    }
                                )
                                Divider()
                            }
                            items(categories) { category ->
                                ListItem(
                                    headlineContent = { Text(category) },
                                    modifier = Modifier.clickable {
                                        selectedCategory = category
                                        step = 3
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                    3 -> {
                        // Template Select (Filtered by Category)
                        val filteredTemplates = if (selectedCategory == null) {
                            templates // Show all or just those without category? Let's show all or allow "Blank"
                        } else {
                            templates.filter { it.category == selectedCategory }
                        }
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.crm_wizard_blank_project)) },
                                    modifier = Modifier.clickable {
                                        selectedTemplate = null
                                        projectName = ""
                                        step = 4
                                    }
                                )
                                Divider()
                            }
                            items(filteredTemplates) { template ->
                                ListItem(
                                    headlineContent = { Text(template.name) },
                                    supportingContent = { template.description?.let { Text(it) } },
                                    modifier = Modifier.clickable {
                                        selectedTemplate = template
                                        projectName = template.name // Auto-fill
                                        step = 4
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                    4 -> {
                        // Details
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text(stringResource(R.string.crm_project_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val clientName = selectedClient?.let { 
                            if (it.tipoCliente == ClientType.PARTICULAR) "${it.firstName} ${it.lastName}" else it.firstName
                        } ?: ""
                        Text(stringResource(R.string.crm_wizard_summary_client, clientName))
                        Text(stringResource(R.string.crm_wizard_summary_template, selectedTemplate?.name ?: stringResource(R.string.general_none)))

                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
                            Button(
                                onClick = { 
                                    if (projectName.isNotBlank() && selectedClient != null) {
                                        onConfirm(selectedClient!!.id, projectName, startDate, endDate, selectedTemplate?.id, selectedCategory)
                                    }
                                },
                                enabled = projectName.isNotBlank()
                            ) { Text(stringResource(R.string.general_create)) }
                        }
                    }
                }
            }
        }
    }
}
