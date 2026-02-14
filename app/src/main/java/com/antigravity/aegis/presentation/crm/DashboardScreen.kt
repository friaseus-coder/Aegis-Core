package com.antigravity.aegis.presentation.crm

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CrmViewModel,
    onNavigateToClients: () -> Unit,
    onNavigateToProject: (Int) -> Unit
) {
    val activeProjects by viewModel.activeRootProjects.collectAsState()
    val allClients by viewModel.allClients.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val categories by viewModel.templateCategories.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
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
            Button(
                onClick = onNavigateToClients,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.dashboard_manage_clients))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.dashboard_active_projects),
                style = MaterialTheme.typography.titleLarge
            )
            
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(project.name, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.dashboard_status_label, project.status), style = MaterialTheme.typography.bodyMedium)
                            project.endDate?.let {
                                Text(stringResource(R.string.dashboard_deadline_label, it), style = MaterialTheme.typography.bodySmall)
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
        }

        if (showCreateDialog) {
            CreateProjectWizard(
                clients = allClients,
                categories = categories,
                templates = templates,
                onDismiss = { showCreateDialog = false },
                onConfirm = { clientId, name, startDate, endDate, templateId ->
                    if (templateId != null) {
                         viewModel.createProjectFromTemplate(templateId, clientId, name, startDate, endDate)
                    } else {
                         viewModel.createProject(clientId, name, "ACTIVE", startDate, endDate)
                    }
                    showCreateDialog = false
                },
                onCreateClient = onNavigateToClients
            )
        }
    }
}

@Composable
fun CreateProjectWizard(
    clients: List<com.antigravity.aegis.data.local.entity.ClientEntity>,
    categories: List<String>,
    templates: List<com.antigravity.aegis.data.local.entity.ProjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Long, Long?, Int?) -> Unit,
    onCreateClient: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedClient by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ClientEntity?>(null) }
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
                        1 -> "Seleccionar Cliente"
                        2 -> "Seleccionar Categoría"
                        3 -> "Seleccionar Plantilla"
                        4 -> "Detalles del Proyecto"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                when(step) {
                    1 -> {
                        // Client Search/Select
                         if (clients.isEmpty()) {
                             Text("No hay clientes. Crea uno primero.")
                             Button(onClick = onCreateClient) { Text("Crear Cliente") }
                         } else {
                             LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                 items(clients) { client ->
                                     ListItem(
                                         headlineContent = { Text("${client.firstName} ${client.lastName}") },
                                         modifier = Modifier.clickable {
                                             selectedClient = client
                                             step = 2
                                         }
                                     )
                                     Divider()
                                 }
                             }
                         }
                    }
                    2 -> {
                        // Category Select
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            item {
                                ListItem(
                                    headlineContent = { Text("Sin Categoría / Personalizado") },
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
                                    headlineContent = { Text("Proyecto en Blanco (Sin Plantilla)") },
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
                            label = { Text("Nombre del Proyecto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cliente: ${selectedClient?.firstName} ${selectedClient?.lastName}")
                        Text("Plantilla: ${selectedTemplate?.name ?: "Ninguna"}")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onDismiss) { Text("Cancelar") }
                            Button(
                                onClick = { 
                                    if (projectName.isNotBlank() && selectedClient != null) {
                                        onConfirm(selectedClient!!.id, projectName, startDate, endDate, selectedTemplate?.id)
                                    }
                                },
                                enabled = projectName.isNotBlank()
                            ) { Text("Crear") }
                        }
                    }
                }
            }
        }
    }
}
