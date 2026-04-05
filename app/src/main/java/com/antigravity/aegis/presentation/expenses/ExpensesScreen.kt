@file:OptIn(ExperimentalMaterial3Api::class)
package com.antigravity.aegis.presentation.expenses

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.presentation.components.SyncBanner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val projects by viewModel.activeProjects.collectAsState()
    val scannedData by viewModel.scannedData.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncingCalendar.collectAsState()
    val showClosedDialog by viewModel.showClosedDialog.collectAsState()
    
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    
    // Import Picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.validateImport(uri)
        }
    }

    // Handling Transfer States
    when (val state = transferState) {
        is ExpensesViewModel.TransferState.Success -> {
            val msg = state.message ?: state.resId?.let { if (state.arg != null) stringResource(it, state.arg) else stringResource(it) } ?: ""
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is ExpensesViewModel.TransferState.Error -> {
            val msg = state.message ?: state.resId?.let { if (state.arg != null) stringResource(it, state.arg) else stringResource(it) } ?: ""
            Toast.makeText(context, stringResource(R.string.general_error_prefix, msg), Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is ExpensesViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text(stringResource(R.string.data_import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text(stringResource(R.string.general_ok)) } }
            )
        }
        is ExpensesViewModel.TransferState.ValidationSuccess -> {
            ImportConfirmationDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onCancel = { viewModel.resetTransferState() }
            )
        }
        else -> {}
    }

    if (showClosedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissClosedDialog() },
            title = { Text(stringResource(R.string.crm_project_close_warning_title)) },
            text = { Text(stringResource(R.string.crm_project_close_warning_message)) },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmClosedUsage() }) {
                    Text(stringResource(R.string.general_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissClosedDialog() }) {
                    Text(stringResource(R.string.general_cancel))
                }
            }
        )
    }

    // Camera Logic
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.processImage(tempImageUri!!)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context, { tempImageUri = it }, { cameraLauncher.launch(it) })
        } else {
            Toast.makeText(context, context.getString(R.string.inventory_scan_label), Toast.LENGTH_SHORT).show()
        }
    }

     // Export Effect
    LaunchedEffect(exportStatus) {
        if (exportStatus != null) {
            Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
            viewModel.clearExportStatus()
        }
    }

    // PDF Share Effect
    val pdfShareUri by viewModel.pdfShareUri.collectAsState()
    LaunchedEffect(pdfShareUri) {
        pdfShareUri?.let { uri ->
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.settings_share_pdf_title)))
            viewModel.clearPdfShareUri()
        }
    }

    var showDateRangePicker by remember { mutableStateOf(false) }
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.shareExpensesReport(start, end)
                            showDateRangePicker = false
                        }
                    }
                ) { Text(stringResource(R.string.general_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text(stringResource(R.string.general_cancel)) }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text(stringResource(R.string.expenses_report_date_range_title), modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (scannedData != null || showAddDialog || selectedExpense != null) {
        AddExpenseDialog(
            projects = projects,
            scannedData = scannedData,
            existingExpense = selectedExpense,
            imageUri = if (scannedData != null) tempImageUri else null,
            onSave = { date, total, base, tax, merchant, category, projectId ->
                 if (selectedExpense != null) {
                     viewModel.updateExpense(selectedExpense!!.copy(
                         date = date,
                         totalAmount = total,
                         baseAmount = base,
                         taxAmount = tax,
                         merchantName = merchant,
                         category = category,
                         projectId = projectId
                     ))
                 } else {
                     viewModel.saveExpense(date, total, base, tax, merchant, if (scannedData != null) tempImageUri else null, category, projectId)
                 }
                 showAddDialog = false
                 selectedExpense = null
            },
            onDismiss = { 
                viewModel.clearScannedData() 
                showAddDialog = false
                selectedExpense = null
            }
        )
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = { viewModel.exportExpenses() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.data_export_csv))
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.data_import_csv))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_new_entry))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Title
            Text(
                text = stringResource(R.string.expenses_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.expenses_report_share_btn))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.expenses_report_share_btn))
                }
                
                Button(
                    onClick = { 
                        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        )
                        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            launchCamera(context, { tempImageUri = it }, { cameraLauncher.launch(it) })
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f),
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.inventory_scan_label))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.inventory_scan_label))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        currencySymbol = currencySymbol,
                        onClick = { selectedExpense = expense },
                        onDistribute = {
                            viewModel.distributeExpense(expense, projects.map { it.id })
                        }
                    )
                }
            }

            SyncBanner(
                pendingCount = pendingSyncCount,
                isSyncing = isSyncing,
                onSyncNow = { viewModel.syncToCalendar() }
            )
        }
    }
}

@Composable
fun AddExpenseDialog(
    projects: List<com.antigravity.aegis.data.local.entity.ProjectEntity>,
    scannedData: com.antigravity.aegis.domain.expenses.OcrManager.ExtractedData?,
    existingExpense: com.antigravity.aegis.data.local.entity.ExpenseEntity? = null,
    imageUri: Uri?,
    onSave: (Long, Double, Double, Double, String, String, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: ExpensesViewModel = hiltViewModel()
    val defaultTax by viewModel.defaultTaxPercent.collectAsState()
    
    var merchant by remember { mutableStateOf(existingExpense?.merchantName ?: "") }
    var totalAmount by remember { mutableStateOf(existingExpense?.totalAmount?.let { String.format("%.2f", it) } ?: scannedData?.totalAmount?.let { String.format("%.2f", it) } ?: "") }
    var baseAmount by remember { mutableStateOf(existingExpense?.baseAmount?.let { String.format("%.2f", it) } ?: "") }
    var taxPercent by remember { mutableStateOf(String.format("%.1f", defaultTax)) }
    var taxAmount by remember { mutableStateOf(existingExpense?.taxAmount?.let { String.format("%.2f", it) } ?: "") }
    
    val merchantSuggestions by viewModel.merchantSuggestions.collectAsState()
    var merchantExpanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(merchant, merchantSuggestions) {
        if (merchant.isBlank()) emptyList()
        else merchantSuggestions.filter { it.contains(merchant, ignoreCase = true) && it != merchant }
    }
    
    // Initial calculation if we have total and it's a new scan
    LaunchedEffect(scannedData, defaultTax) {
        if (scannedData != null && baseAmount.isEmpty()) {
            val t = scannedData.totalAmount ?: 0.0
            val p = defaultTax
            val b = t / (1 + (p / 100.0))
            baseAmount = String.format("%.2f", b)
            taxAmount = String.format("%.2f", t - b)
        }
    }

    fun updateFromTotal(total: String) {
        val t = total.replace(",", ".").toDoubleOrNull() ?: 0.0
        val p = taxPercent.replace(",", ".").toDoubleOrNull() ?: 0.0
        val b = t / (1 + (p / 100.0))
        baseAmount = String.format("%.2f", b)
        taxAmount = String.format("%.2f", t - b)
    }
    
    fun updateFromBase(base: String) {
        val b = base.replace(",", ".").toDoubleOrNull() ?: 0.0
        val p = taxPercent.replace(",", ".").toDoubleOrNull() ?: 0.0
        val t = b * (1 + (p / 100.0))
        totalAmount = String.format("%.2f", t)
        taxAmount = String.format("%.2f", t - b)
    }

    val categories = listOf(
        stringResource(R.string.expense_cat_material),
        stringResource(R.string.expense_cat_transport),
        stringResource(R.string.expense_cat_office),
        stringResource(R.string.expense_cat_food),
        stringResource(R.string.expense_cat_other)
    )
    var category by remember { mutableStateOf(existingExpense?.category ?: categories[0]) }
    var selectedProject by remember { mutableStateOf(projects.find { it.id == existingExpense?.projectId }) }
    var date by remember { mutableStateOf(existingExpense?.date ?: scannedData?.date ?: System.currentTimeMillis()) }
    var expandedCat by remember { mutableStateOf(false) }
    var expandedProj by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        title = { 
            Text(
                when {
                    existingExpense != null -> stringResource(R.string.expenses_edit_expense_title)
                    scannedData != null -> stringResource(R.string.expenses_review_ticket_title)
                    else -> stringResource(R.string.ui_new_entry)
                }
            )
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = merchantExpanded && filteredSuggestions.isNotEmpty(),
                        onExpandedChange = { merchantExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = merchant,
                            onValueChange = { 
                                merchant = it
                                merchantExpanded = true
                            },
                            label = { Text(stringResource(R.string.expenses_merchant_label)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = merchantExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = merchantExpanded && filteredSuggestions.isNotEmpty(),
                            onDismissRequest = { merchantExpanded = false }
                        ) {
                            filteredSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        merchant = suggestion
                                        merchantExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = baseAmount,
                            onValueChange = { 
                                baseAmount = it
                                updateFromBase(it)
                            },
                            label = { Text(stringResource(R.string.expenses_label_base_amount)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = taxPercent,
                            onValueChange = { 
                                taxPercent = it
                                updateFromTotal(totalAmount) 
                            },
                            label = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taxAmount,
                            onValueChange = { taxAmount = it },
                            label = { Text(stringResource(R.string.expenses_label_tax_amount)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { 
                                totalAmount = it
                                updateFromTotal(it)
                            },
                            label = { Text(stringResource(R.string.expenses_amount_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Dropdown
                    Text(stringResource(R.string.expenses_merchant_label), style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedCat = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(category)
                        }
                        DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expandedCat = false })
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Project Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedProj = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedProject?.name ?: stringResource(R.string.expense_general_no_project))
                        }
                        DropdownMenu(expanded = expandedProj, onDismissRequest = { expandedProj = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.expense_general_no_project)) }, onClick = { selectedProject = null; expandedProj = false })
                            projects.forEach { proj ->
                                DropdownMenuItem(text = { Text(proj.name) }, onClick = { selectedProject = proj; expandedProj = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                 onClick = {
                     onSave(
                         date, 
                         totalAmount.replace(",", ".").toDoubleOrNull() ?: 0.0,
                         baseAmount.replace(",", ".").toDoubleOrNull() ?: 0.0,
                         taxAmount.replace(",", ".").toDoubleOrNull() ?: 0.0,
                         merchant, 
                         category, 
                         selectedProject?.id
                     )
                 }
            ) { Text(stringResource(R.string.general_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@Composable
fun ExpenseCard(
    expense: com.antigravity.aegis.data.local.entity.ExpenseEntity, 
    currencySymbol: String,
    onClick: () -> Unit,
    onDistribute: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = expense.merchantName ?: stringResource(R.string.ui_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${expense.category} • ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(expense.date))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = currencySymbol + String.format("%.2f", expense.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (expense.projectId == null && expense.status != "Distributed") {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                TextButton(
                    onClick = onDistribute,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.settings_distribute_button))
                }
            } else if (expense.status == "Distributed") {
                 Text(stringResource(R.string.settings_distributed_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

private fun launchCamera(context: android.content.Context, onUriCreated: (Uri) -> Unit, onLaunch: (Uri) -> Unit) {
    try {
        val storageDir = context.cacheDir
        val imageFile = File.createTempFile("scan_${System.currentTimeMillis()}", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        onUriCreated(uri)
        onLaunch(uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.general_error_prefix, e.message ?: "Camera Error"), Toast.LENGTH_SHORT).show()
    }
}
