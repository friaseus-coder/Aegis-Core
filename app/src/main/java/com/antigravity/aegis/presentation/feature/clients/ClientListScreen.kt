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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    viewModel: ClientListViewModel = hiltViewModel(),
    onNavigateToClientDetail: (Int) -> Unit,
    onNavigateToClientCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AegisTopAppBar(
                title = stringResource(R.string.clients_title)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToClientCreate) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_client_fab))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar (min 3 letras)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                maxLines = 1,
                trailingIcon = {
                     if (uiState.searchQuery.isNotEmpty()) {
                         IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                             Icon(Icons.Default.Clear, "Limpiar")
                         }
                     } else {
                         Icon(Icons.Default.Search, "Buscar")
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
                     label = { Text("Todos") }
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(
                     selected = uiState.filterType == ClientType.PARTICULAR, 
                     onClick = { 
                         viewModel.onFilterSelected(if (uiState.filterType == ClientType.PARTICULAR) null else ClientType.PARTICULAR) 
                     }, 
                     label = { Text("Particulares") }
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 FilterChip(
                     selected = uiState.filterType == ClientType.EMPRESA, 
                     onClick = { 
                         viewModel.onFilterSelected(if (uiState.filterType == ClientType.EMPRESA) null else ClientType.EMPRESA) 
                     }, 
                     label = { Text("Empresas") }
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
                                    Text(
                                        text = if (!detalle.isNullOrEmpty()) "${client.tipoCliente} • $detalle" else client.tipoCliente.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = client.categoria.name,
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
                                Text(stringResource(R.string.no_clients))
                            }
                        }
                    }
                }
            }
        }
    }
}
