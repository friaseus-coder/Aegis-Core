package com.antigravity.aegis.presentation.crm

import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.presentation.components.AegisTopAppBar

// ...

@Composable
fun BudgetEditorScreen(
    projectId: Int? = null,
    quoteId: Int? = null,
    viewModel: BudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val budgetState by viewModel.budgetState.collectAsState()
    val quote by viewModel.currentQuote.collectAsState()
    val lines by viewModel.lines.collectAsState()
    
    val pdfFile by viewModel.pdfFile.collectAsState()
    val context = LocalContext.current
    
    var showAddLineDialog by remember { mutableStateOf(false) }

    val shareTitle = stringResource(R.string.share_pdf_title)

    LaunchedEffect(projectId, quoteId) {
        if (quoteId != null && quoteId != 0) {
            viewModel.loadBudget(quoteId)
        } else if (projectId != null && projectId != 0) {
            viewModel.initNewBudget(projectId)
        }
    }

    LaunchedEffect(pdfFile) {
        if (pdfFile != null) {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile!!
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, shareTitle))
            viewModel.clearPdfFile()
        }
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = { viewModel.generatePdf() }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_content_desc))
                    }
                    IconButton(onClick = { viewModel.saveBudget() }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save_content_desc))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddLineDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_line_fab))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (budgetState is BudgetViewModel.BudgetState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (quote != null) {
                OutlinedTextField(
                    value = quote!!.title,
                    onValueChange = { viewModel.updateQuoteDetails(it, quote!!.description) },
                    label = { Text(stringResource(R.string.budget_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quote!!.description,
                    onValueChange = { viewModel.updateQuoteDetails(quote!!.title, it) },
                    label = { Text(stringResource(R.string.budget_desc_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.budget_lines_title), style = MaterialTheme.typography.titleMedium)
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(lines) { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.description, style = MaterialTheme.typography.bodyLarge)
                                Text("${line.quantity} x €${line.unitPrice} + ${(line.taxRate * 100).toInt()}% IVA", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("€${(line.quantity * line.unitPrice) * (1 + line.taxRate)}", style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { viewModel.removeLine(line) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_content_desc))
                            }
                        }
                        Divider()
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.total_amount_label) + ": €${quote!!.totalAmount}", 
                    style = MaterialTheme.typography.headlineSmall, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            } else {
                 Text("Error loading budget or no quote selected.")
            }
        }
        
        if (showAddLineDialog) {
            AddLineDialog(
                onDismiss = { showAddLineDialog = false },
                onConfirm = { desc, qty, price, tax ->
                    viewModel.addLine(desc, qty, price, tax)
                    showAddLineDialog = false
                }
            )
        }
    }
}

@Composable
fun AddLineDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Double, Double) -> Unit) {
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("0") }
    var tax by remember { mutableStateOf("21") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_line_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.line_description_label)) })
                Row {
                    OutlinedTextField(
                        value = quantity, 
                        onValueChange = { quantity = it }, 
                        label = { Text(stringResource(R.string.quantity_label)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = { Text(stringResource(R.string.price_label)) },
                         modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                // Tax selector could be improved
                 OutlinedTextField(
                    value = tax, 
                    onValueChange = { tax = it }, 
                    label = { Text(stringResource(R.string.tax_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: 1.0
                val p = price.toDoubleOrNull() ?: 0.0
                val t = (tax.toDoubleOrNull() ?: 0.0) / 100.0
                if (description.isNotBlank()) onConfirm(description, q, p, t)
            }) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) } }
    )
}
