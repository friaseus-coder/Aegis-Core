package com.antigravity.aegis.presentation.crm

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.DocumentEntity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    viewModel: CrmViewModel,
    onNavigateToProject: (Int) -> Unit,
    onEditClient: () -> Unit
) {
    val client by viewModel.selectedClient.collectAsState()
    val projects by viewModel.clientProjects.collectAsState()
    val documents by viewModel.clientDocuments.collectAsState()
    val context = LocalContext.current
    
    // Launchers
    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
             viewModel.uploadDocument(uri)
        }
    }
    
    // We need state to know which document we are exporting
    var documentToExport by remember { mutableStateOf<DocumentEntity?>(null) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && documentToExport != null) {
            viewModel.exportDocument(documentToExport!!, uri)
            documentToExport = null
        }
    }

    if (client == null) {
        Text("Client not found")
        return
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = onEditClient) {
                         Icon(Icons.Default.Edit, contentDescription = "Edit Client")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Header Info
            item {
                ClientHeader(client!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Metrics (Placeholders)
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricCard("Proyectos Activos", "${projects.filter { it.status == com.antigravity.aegis.data.local.entity.ProjectStatus.ACTIVE }.size}")
                    MetricCard("Presupuestos", "€ 0.00") // Placeholder
                    MetricCard("Horas Mes", "0h") // Placeholder
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Action Buttons
            item {
                ActionButtonsRow(client!!, context)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Projects List
            item {
                Text("Proyectos", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

                items(projects) { project ->
                    ListItem(
                        headlineContent = { Text(project.name) },
                        supportingContent = { Text(project.status.name) },
                        leadingContent = { Icon(Icons.Default.Work, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToProject(project.id) }
                    )
                    Divider()
                }
            
            // 5. Documents Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Documentos", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { uploadLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Add Document")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(documents) { doc ->
                ListItem(
                    headlineContent = { Text(doc.originalName) },
                    supportingContent = { Text("${doc.mimeType} - ${doc.size / 1024} KB") },
                    leadingContent = { Icon(Icons.Default.Description, contentDescription = null) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { 
                                documentToExport = doc
                                exportLauncher.launch(doc.originalName)
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Export")
                            }
                            IconButton(onClick = { viewModel.deleteDocument(doc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ClientHeader(client: ClientEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = client.tipoCliente, style = MaterialTheme.typography.labelSmall)
            Text(
                text = if (client.tipoCliente == "Particular") "${client.firstName} ${client.lastName}" else client.firstName, 
                style = MaterialTheme.typography.headlineMedium
            )
            
            client.personaContacto?.let { 
                Text(text = "Contacto: $it", style = MaterialTheme.typography.bodyMedium)
            }
            if (!client.nifCif.isNullOrBlank()) {
                Text(text = "${if (client.tipoCliente == "Empresa") "CIF" else "NIF"}: ${client.nifCif}", style = MaterialTheme.typography.bodySmall)
            }
            if (client.tipoCliente == "Empresa" && !client.razonSocial.isNullOrBlank()) {
                Text(text = "Razón Social: ${client.razonSocial}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Categoría: ${client.categoria}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String) {
    Card(modifier = Modifier.width(100.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun ActionButtonsRow(client: ClientEntity, context: android.content.Context) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (!client.phone.isNullOrBlank()) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
            IconButton(onClick = {
                  val url = "https://api.whatsapp.com/send?phone=${client.phone}"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                  context.startActivity(intent)
            }) {
                Icon(Icons.Default.Message, contentDescription = "WhatsApp") // Use appropriate icon if available
            }
        }
        
        if (!client.email.isNullOrBlank()) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.email}"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Email, contentDescription = "Email")
            }
        }

        val address = listOfNotNull(client.calle, client.numero, client.poblacion).joinToString(", ")
        if (address.isNotBlank()) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Map, contentDescription = "Navigate")
            }
        }
    }
}
