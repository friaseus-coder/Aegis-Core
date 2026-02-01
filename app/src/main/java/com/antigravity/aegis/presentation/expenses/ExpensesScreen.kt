package com.antigravity.aegis.presentation.expenses

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R

import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val scannedData by viewModel.scannedData.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    
    val context = LocalContext.current
    
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
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is ExpensesViewModel.TransferState.Error -> {
            Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is ExpensesViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text(stringResource(R.string.import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text(stringResource(R.string.ok_button)) } }
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

    // Helper to create temp file
    fun createImageFile(): Uri {
        val storageDir = context.cacheDir
        val imageFile = File.createTempFile("scan_${System.currentTimeMillis()}", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    // Export Effect
    LaunchedEffect(exportStatus) {
        if (exportStatus != null) {
            Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
            // ToDo: Launch Share Intent if path is valid file...
            viewModel.clearExportStatus()
        }
    }

    if (scannedData != null) {
        // Show Form to Review Scanned Data
        ReviewScanDialog(
            data = scannedData!!,
            imageUri = tempImageUri,
            onSave = { amount, merchant -> 
                 viewModel.saveExpense(
                     date = scannedData!!.date ?: System.currentTimeMillis(),
                     amount = amount,
                     merchant = merchant,
                     imageUri = tempImageUri
                 )
            },
            onDismiss = { viewModel.clearScannedData() }
        )
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = { viewModel.exportExpenses() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Export CSV")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Import CSV")
                    }
                }
            )
        },
        // ...
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

            // Header Actions
            Button(
                onClick = { viewModel.exportQuarter() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.export_quarter))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseCard(expense)
                }
            }
        }
    }
}

@Composable
fun ReviewScanDialog(
    data: com.antigravity.aegis.domain.expenses.OcrManager.ExtractedData,
    imageUri: Uri?,
    onSave: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(data.totalAmount?.toString() ?: "") }
    var merchant by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.review_ticket)) },
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
                    label = { Text(stringResource(R.string.merchant_concept)) }
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                if (data.date != null) {
                    Text(
                        text = "Date detected: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(data.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                 onClick = {
                     onSave(amount.toDoubleOrNull() ?: 0.0, merchant)
                 }
            ) { Text(stringResource(R.string.save_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}

@Composable
fun ExpenseCard(expense: com.antigravity.aegis.data.model.ExpenseEntity) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
         modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.merchantName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = String.format("$%.2f", expense.totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
