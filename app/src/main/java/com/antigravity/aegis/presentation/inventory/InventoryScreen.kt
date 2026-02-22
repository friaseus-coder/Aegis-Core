package com.antigravity.aegis.presentation.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.domain.inventory.BarcodeAnalyzer
import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
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
        is InventoryViewModel.TransferState.Success -> {
            val msg = state.message ?: state.resId?.let { if (state.arg != null) stringResource(it, state.arg) else stringResource(it) } ?: ""
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is InventoryViewModel.TransferState.Error -> {
            val msg = state.message ?: state.resId?.let { if (state.arg != null) stringResource(it, state.arg) else stringResource(it) } ?: ""
            Toast.makeText(context, stringResource(R.string.general_error_prefix, msg), Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is InventoryViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text(stringResource(R.string.data_import_errors_title)) },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text(stringResource(R.string.general_ok)) } }
            )
        }
        is InventoryViewModel.TransferState.ValidationSuccess -> {
            ImportConfirmationDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onCancel = { viewModel.resetTransferState() }
            )
        }
        else -> {}
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                actions = {
                    IconButton(onClick = { viewModel.exportProducts() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.ui_export_csv))
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.ui_import_csv))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text(stringResource(R.string.inventory_stock_list_title)) },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text(stringResource(R.string.inventory_scan_label)) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_new_entry))
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (selectedTab == 0) {
                 Text(
                    text = stringResource(R.string.inventory_stock_list_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(product, viewModel)
                    }
                }
            } else {
                BarcodeScannerView(viewModel)
            }
        }
    }

    if (showAddDialog || scanState is InventoryViewModel.ScanResult.NotFound) {
        val scCode = (scanState as? InventoryViewModel.ScanResult.NotFound)?.barcode
        AddProductDialog(
            scannedCode = scCode,
            onSave = { name, code, stock, minStock ->
                viewModel.createProduct(code, name, 0.0, stock, minStock)
                showAddDialog = false
            },
            onDismiss = {
                viewModel.resetScan()
                showAddDialog = false
            }
        )
    }
    
    // Scan Found logic
    if (scanState is InventoryViewModel.ScanResult.Found) {
        val product = (scanState as InventoryViewModel.ScanResult.Found).product
        AlertDialog(
            onDismissRequest = { viewModel.resetScan() },
            title = { Text(product.name) },
            text = {
                Column {
                    Text("SKU: ${product.barcode}")
                    Text("${stringResource(R.string.inventory_label_stock)}: ${product.quantity}")
                }
            },
            confirmButton = {
                Row {
                   TextButton(onClick = { viewModel.updateQuantity(product, -1) }) { Text("-1") }
                   TextButton(onClick = { viewModel.updateQuantity(product, 1) }) { Text("+1") }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetScan() }) { Text(stringResource(R.string.general_ok)) }
            }
        )
    }
}

@Composable
fun ProductCard(product: ProductEntity, viewModel: InventoryViewModel) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.padding(8.dp).fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("SKU: ${product.barcode}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${product.quantity}/${product.minQuantity}",
                    color = if (product.quantity <= product.minQuantity) Color.Red else Color.Unspecified
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { viewModel.updateQuantity(product, -1) }) { Icon(Icons.Default.ArrowDownward, "-1") }
                IconButton(onClick = { viewModel.updateQuantity(product, 1) }) { Icon(Icons.Default.ArrowUpward, "+1") }
            }
        }
    }
}

@Composable
fun BarcodeScannerView(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it }
    )
    
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }
    
    if (hasCameraPermission) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                
                val preview = Preview.Builder().build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    BarcodeAnalyzer { code ->
                        viewModel.onBarcodeDetected(code)
                    }
                )
                
                try {
                    cameraProviderFuture.get().bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AddProductDialog(scannedCode: String?, onSave: (String, String, Int, Int) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(scannedCode ?: "") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.inventory_new_product_title)) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.inventory_label_name)) })
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text(stringResource(R.string.inventory_label_sku)) })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text(stringResource(R.string.inventory_label_stock)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text(stringResource(R.string.inventory_label_min_stock)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, code, stock.toIntOrNull() ?: 0, minStock.toIntOrNull() ?: 0) }) {
                Text(stringResource(R.string.general_save))
            }
        }
    )
}
