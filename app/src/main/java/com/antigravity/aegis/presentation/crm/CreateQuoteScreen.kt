package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteScreen(
    viewModel: QuoteKanbanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Line Items State
    val budgetLines = remember { mutableStateListOf<com.antigravity.aegis.data.model.BudgetLineEntity>() }
    
    // New Item Inputs
    var newItemDesc by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    
    val clients by viewModel.allClients.collectAsState()
    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var expandedClientDropdown by remember { mutableStateOf(false) }

    // Constants
    val taxRate = 0.21

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
                            if (selectedClient != null && title.isNotBlank() && budgetLines.isNotEmpty()) {
                                viewModel.createQuote(
                                    title = title,
                                    clientId = selectedClient!!.id,
                                    lines = budgetLines.toList(),
                                    description = description
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = selectedClient != null && title.isNotBlank() && budgetLines.isNotEmpty()
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
            // Client and Info Section
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
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.quote_description_label)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 3
            )
            
            Divider()
            
            // Add Item Section
            Text("Añadir Partida", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newItemDesc,
                    onValueChange = { newItemDesc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = newItemQty,
                    onValueChange = { if(it.all { c -> c.isDigit() || c == '.'}) newItemQty = it },
                    label = { Text("Cant.") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                 OutlinedTextField(
                    value = newItemPrice,
                    onValueChange = { if(it.all { c -> c.isDigit() || c == '.'}) newItemPrice = it },
                    label = { Text("Precio (€)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Button(
                onClick = {
                    val qty = newItemQty.toDoubleOrNull()
                    val price = newItemPrice.toDoubleOrNull()
                    if (newItemDesc.isNotBlank() && qty != null && price != null) {
                        budgetLines.add(
                            com.antigravity.aegis.data.model.BudgetLineEntity(
                                quoteId = 0, // Temp
                                description = newItemDesc,
                                quantity = qty,
                                unitPrice = price,
                                taxRate = taxRate
                            )
                        )
                        newItemDesc = ""
                        newItemQty = ""
                        newItemPrice = ""
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = newItemDesc.isNotBlank() && newItemQty.isNotBlank() && newItemPrice.isNotBlank()
            ) {
                Text("Añadir")
            }
            
            // List of Items
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(budgetLines.size) { index ->
                    val line = budgetLines[index]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(line.description, modifier = Modifier.weight(2f))
                        Text("${line.quantity} x ${line.unitPrice}€", modifier = Modifier.weight(1f))
                        Text("${"%.2f".format(line.quantity * line.unitPrice)}€", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { budgetLines.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                             Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                    Divider()
                }
            }
            
            // Total
            val subtotal = budgetLines.sumOf { it.quantity * it.unitPrice }
            val totalTax = subtotal * taxRate
            val total = subtotal + totalTax
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Subtotal: ${"%.2f".format(subtotal)}€")
                    Text("IVA (21%): ${"%.2f".format(totalTax)}€")
                    Text("TOTAL: ${"%.2f".format(total)}€", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
