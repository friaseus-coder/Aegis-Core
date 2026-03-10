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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    
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

    // Camera Logic
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.processImage(tempImageUri!!)
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val storageDir = context.cacheDir
            val imageFile = File.createTempFile("scan_${System.currentTimeMillis()}", ".jpg", storageDir)
            tempImageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
            tempImageUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para escanear tickets", Toast.LENGTH_SHORT).show()
        }
    }

     // Export Effect
    LaunchedEffect(exportStatus) {
        if (exportStatus != null) {
            Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
            viewModel.clearExportStatus()
        }
    }

    if (scannedData != null || showAddDialog) {
        AddExpenseDialog(
            projects = projects,
            scannedData = scannedData,
            imageUri = if (scannedData != null) tempImageUri else null,
            onSave = { date, amount, merchant, category, projectId ->
                 viewModel.saveExpense(date, amount, merchant, if (scannedData != null) tempImageUri else null, category, projectId)
                 showAddDialog = false
            },
            onDismiss = { 
                viewModel.clearScannedData() 
                showAddDialog = false
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
                    onClick = { viewModel.exportQuarter() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.expenses_export_quarter_button))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.expenses_export_quarter_button))
                }
                
                Button(
                    onClick = { 
                        // Launch Camera with permission check
                        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        )
                        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val storageDir = context.cacheDir
                            val imageFile = File.createTempFile("scan_${System.currentTimeMillis()}", ".jpg", storageDir)
                            tempImageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
                            tempImageUri?.let { cameraLauncher.launch(it) }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        currencySymbol = currencySymbol,
                        onDistribute = {
                            viewModel.distributeExpense(expense, projects.map { it.id })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    projects: List<com.antigravity.aegis.data.local.entity.ProjectEntity>,
    scannedData: com.antigravity.aegis.domain.expenses.OcrManager.ExtractedData?,
    imageUri: Uri?,
    onSave: (Long, Double, String, String, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(scannedData?.totalAmount?.toString() ?: "") }
    
    val categories = listOf(
        stringResource(R.string.expense_cat_material),
        stringResource(R.string.expense_cat_transport),
        stringResource(R.string.expense_cat_office),
        stringResource(R.string.expense_cat_food),
        stringResource(R.string.expense_cat_other)
    )
    var category by remember { mutableStateOf(categories[0]) }
    var selectedProject by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    var date by remember { mutableStateOf(scannedData?.date ?: System.currentTimeMillis()) }
    var expandedCat by remember { mutableStateOf(false) }
    var expandedProj by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (scannedData != null) stringResource(R.string.expenses_review_ticket_title) else stringResource(R.string.ui_new_entry)) },
        text = {
            Column {
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
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text(stringResource(R.string.expenses_merchant_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.expenses_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Dropdown
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
        },
        confirmButton = {
            Button(
                 onClick = {
                     onSave(date, amount.toDoubleOrNull() ?: 0.0, merchant, category, selectedProject?.id)
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
    onDistribute: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
         modifier = Modifier.fillMaxWidth()
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
