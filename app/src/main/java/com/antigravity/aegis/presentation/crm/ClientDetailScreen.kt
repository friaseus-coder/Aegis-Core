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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: CrmViewModel,
    onNavigateToProject: (Int) -> Unit
) {
    val client by viewModel.selectedClient.collectAsState()
    val projects by viewModel.clientProjects.collectAsState()
    var showAddProjectDialog by remember { mutableStateOf(false) }

    if (client == null) {
        // Fallback or Loading
        Text(stringResource(R.string.client_not_found))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { 
                Text(
                    if (client!!.tipoCliente == "Particular") "${client!!.firstName} ${client!!.lastName}" 
                    else client!!.firstName 
                ) 
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddProjectDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_project_fab))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Client Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val address = listOfNotNull(client!!.calle, client!!.numero, client!!.piso, client!!.codigoPostal, client!!.poblacion).joinToString(", ")
                    
                    Text("Tipo: ${client!!.tipoCliente} - ${client!!.categoria}")
                    if (!client!!.nifCif.isNullOrEmpty()) {
                         Text("${if (client!!.tipoCliente == "Empresa") "CIF" else "NIF"}: ${client!!.nifCif}")
                    }
                    if (address.isNotBlank()) {
                         Text("Dirección: $address")
                    }
                    Text(stringResource(R.string.client_email_label, client!!.email ?: stringResource(R.string.not_available_short)))
                    Text(stringResource(R.string.client_phone_label, client!!.phone ?: stringResource(R.string.not_available_short)))
                    Text("Notas: ${client!!.notas ?: stringResource(R.string.not_available_short)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.projects_section_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(projects) { project ->
                    ListItem(
                        headlineContent = { Text(project.name) },
                        supportingContent = { Text(project.status.name) },
                        modifier = Modifier.clickable { onNavigateToProject(project.id) }
                    )
                    Divider()
                }
            }
        }

        if (showAddProjectDialog) {
            AddProjectDialog(
                onDismiss = { showAddProjectDialog = false },
                onConfirm = { name, status, deadline ->
                    viewModel.createProject(client!!.id, name, status, System.currentTimeMillis(), deadline)
                    showAddProjectDialog = false
                }
            )
        }
    }
}

@Composable
fun AddProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String, Long?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }
    // Simplified deadline input for now
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_project_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.project_name_label)) })
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) onConfirm(name, status, null) 
            }) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}
