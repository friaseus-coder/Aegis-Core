package com.antigravity.aegis.presentation.mileage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScreen(
    viewModel: MileageViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val userConfig by viewModel.userConfig.collectAsState()
    val logs by viewModel.logs.collectAsState()
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
        is MileageViewModel.TransferState.Success -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is MileageViewModel.TransferState.Error -> {
            Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is MileageViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text("Import Errors") },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text("OK") } }
            )
        }
        is MileageViewModel.TransferState.ValidationSuccess -> {
            ImportConfirmationDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onCancel = { viewModel.resetTransferState() }
            )
        }
        else -> {}
    }

    // Form inputs
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startOdo by remember { mutableStateOf("") }
    var endOdo by remember { mutableStateOf("") }
    
    // Settings State
    var showSettings by remember { mutableStateOf(false) }
    var tempPrice by remember { mutableStateOf("") }

    // ... (LaunchEffect code can stay)

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                OutlinedTextField(
                    value = tempPrice,
                    onValueChange = { tempPrice = it },
                    label = { Text("Price per Km (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updatePricePerKm(tempPrice.toDoubleOrNull() ?: 0.0)
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mileage Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                     IconButton(onClick = { viewModel.exportMileage() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Export CSV")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Import CSV")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Calculator Card
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("New Trip Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = origin, onValueChange = { origin = it },
                            label = { Text("Origin") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = destination, onValueChange = { destination = it },
                            label = { Text("Destination") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startOdo, onValueChange = { startOdo = it },
                            label = { Text("Start Odo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endOdo, onValueChange = { endOdo = it },
                            label = { Text("End Odo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Pre-calculation display
                    val start = startOdo.toDoubleOrNull() ?: 0.0
                    val end = endOdo.toDoubleOrNull() ?: 0.0
                    val dist = (end - start).coerceAtLeast(0.0)
                    val price = userConfig?.pricePerKm ?: 0.0
                    val cost = dist * price
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Distance: $dist km")
                            Text("Est. Cost: ${String.format("€%.2f", cost)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {
                                viewModel.saveTrip(
                                    date = System.currentTimeMillis(),
                                    origin = origin,
                                    destination = destination,
                                    vehicle = "Default Car",
                                    startOdo = start,
                                    endOdo = end,
                                    currentPrice = price
                                )
                                // Clear Fields
                                origin = ""
                                destination = ""
                                startOdo = endOdo // Auto-chaining
                                endOdo = ""
                            },
                            enabled = dist > 0
                        ) {
                            Text("Log Trip")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Trips", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.exportAnnualReport() }) {
                    Text("Export Annual CSV")
                }
            }
            
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    MileageItem(log)
                }
            }
        }
    }
}

@Composable
fun MileageItem(log: com.antigravity.aegis.data.model.MileageLogEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${log.origin} ➝ ${log.destination}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(log.date)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${log.distanceKm} km")
                Text(
                    text = String.format("€%.2f", log.calculatedCost),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
