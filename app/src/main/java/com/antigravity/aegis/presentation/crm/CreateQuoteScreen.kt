package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.data.model.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteScreen(
    viewModel: QuoteKanbanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    
    val clients by viewModel.allClients.collectAsState()
    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var expandedClientDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_quote_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_content_desc))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedClient != null && title.isNotBlank()) {
                                viewModel.createQuote(
                                    title = title,
                                    clientId = selectedClient!!.id,
                                    amount = amountText.toDoubleOrNull() ?: 0.0,
                                    description = description
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = selectedClient != null && title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_content_desc))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client Selection
            ExposedDropdownMenuBox(
                expanded = expandedClientDropdown,
                onExpandedChange = { expandedClientDropdown = !expandedClientDropdown }
            ) {
                OutlinedTextField(
                    value = selectedClient?.let { if (it.tipoCliente == "Particular") "${it.firstName} ${it.lastName}" else it.firstName } ?: stringResource(R.string.select_client_placeholder),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.client_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientDropdown) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedClientDropdown,
                    onDismissRequest = { expandedClientDropdown = false }
                ) {
                    if (clients.isEmpty()) {
                         DropdownMenuItem(
                            text = { Text("No clients found") },
                            onClick = { expandedClientDropdown = false }
                        )
                    } else {
                        clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(if (client.tipoCliente == "Particular") "${client.firstName} ${client.lastName}" else client.firstName) },
                                onClick = {
                                    selectedClient = client
                                    expandedClientDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.quote_title_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountText = it },
                label = { Text(stringResource(R.string.total_amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.quote_description_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 10
            )
        }
    }
}
