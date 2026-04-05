package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.domain.model.ClientCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: CrmViewModel,
    onNavigateToProject: (Int) -> Unit
) {
    val client by viewModel.selectedClient.collectAsState()
    val projects by viewModel.clientProjects.collectAsState()
    val sessions by viewModel.clientSessions.collectAsState()
    val templates by viewModel.templates.collectAsState()
    var showAddProjectDialog by remember { mutableStateOf(false) }

    if (client == null) {
        // Fallback or Loading
        Text(stringResource(R.string.crm_client_not_found))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { 
                Text(
                    if (client!!.tipoCliente == ClientType.PARTICULAR) "${client!!.firstName} ${client!!.lastName}" 
                    else client!!.firstName 
                ) 
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddProjectDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.crm_project_add_button))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Client Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val address = listOfNotNull(client!!.address?.calle, client!!.address?.numero, client!!.address?.piso, client!!.address?.codigoPostal, client!!.address?.poblacion).joinToString(", ")
                    
                    Text(stringResource(R.string.crm_client_detail_type, client!!.tipoCliente.name, client!!.categoria.name))
                    if (!client!!.nifCif.isNullOrEmpty()) {
                         Text(stringResource(if (client!!.tipoCliente == ClientType.EMPRESA) R.string.crm_client_detail_cif else R.string.crm_client_detail_nif, client!!.nifCif!!))
                    }

                    if (address.isNotBlank()) {
                         Text(stringResource(R.string.crm_client_detail_address, address))
                    }
                    Text(stringResource(R.string.crm_client_detail_email, client!!.email ?: stringResource(R.string.general_na)))
                    Text(stringResource(R.string.crm_client_detail_phone, client!!.phone ?: stringResource(R.string.general_na)))
                    Text(stringResource(R.string.crm_client_detail_notes, client!!.notas ?: stringResource(R.string.general_na)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.crm_projects_section_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                // Section: Projects
                items(projects, key = { it.id }) { project ->
                    ListItem(
                        headlineContent = { Text(project.name) },
                        supportingContent = { Text(project.status) },
                        modifier = Modifier.clickable { onNavigateToProject(project.id) }
                    )
                    HorizontalDivider()
                }

                // Section: Sessions
                if (sessions.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.crm_session_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(sessions, key = { "session_${it.id}" }) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.date)),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(session.duration, style = MaterialTheme.typography.labelMedium)
                                }
                                Text("${session.location} (Proyecto: ${projects.find { it.id == session.projectId }?.name ?: "Desconocido"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(R.string.crm_session_notes) + ":", style = MaterialTheme.typography.labelSmall)
                                Text(session.notes, style = MaterialTheme.typography.bodyMedium)
                                if (session.nextSessionDate != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.crm_session_next_date) + ": " + 
                                            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.nextSessionDate)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddProjectDialog) {
            AddProjectDialog(
                templates = templates,
                onDismiss = { showAddProjectDialog = false },
                onConfirm = { name, status, deadline, templateId ->
                    if (templateId != null) {
                        viewModel.createProjectFromTemplate(templateId, client!!.id, name, System.currentTimeMillis(), deadline)
                    } else {
                        viewModel.createProject(client!!.id, name, status, System.currentTimeMillis(), deadline)
                    }
                    showAddProjectDialog = false
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    templates: List<com.antigravity.aegis.data.local.entity.ProjectEntity>,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Long?, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }
    var selectedTemplate by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_project_new_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Template Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTemplate?.name ?: stringResource(R.string.crm_wizard_blank_project),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.crm_project_template_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.crm_wizard_blank_project)) },
                            onClick = { 
                                selectedTemplate = null
                                expanded = false
                            }
                        )
                        templates.forEach { template ->
                            DropdownMenuItem(
                                text = { Text(template.name) },
                                onClick = { 
                                    selectedTemplate = template
                                    if (name.isBlank()) name = template.name // Auto-fill name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(stringResource(R.string.crm_project_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) onConfirm(name, status, null, selectedTemplate?.id) 
            }) {
                Text(stringResource(R.string.general_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}
