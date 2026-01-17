package com.antigravity.aegis.presentation.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.data.model.ProductEntity
import com.antigravity.aegis.domain.inventory.BarcodeAnalyzer
import java.util.concurrent.Executors

import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward

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
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is InventoryViewModel.TransferState.Error -> {
            Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is InventoryViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text("Import Errors") },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text("OK") } }
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
    
    // Tab State: 0 = List, 1 = Scan
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Control") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportProducts() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Export CSV")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Import CSV")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Stock List") },
                    icon = { Text("📋") } // Placeholder icon
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Scan") },
                    icon = { Text("📷") } // Placeholder icon
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (selectedTab == 0) {
                InventoryList(products)
            } else {
                ScannerView(
                    scanState = scanState,
                    onBarcodeDetected = { viewModel.onBarcodeDetected(it) },
                    onResetScan = { viewModel.resetScan() },
                    onCreateProduct = { code, name, price, qty, min -> 
                        viewModel.createProduct(code, name, price, qty, min)
                    },
                    onUpdateQuantity = { product, delta ->
                        viewModel.updateQuantity(product, delta)
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryList(products: List<ProductEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (product.quantity < product.minQuantity) 
                        MaterialTheme.colorScheme.errorContainer 
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("Barcode: ${product.barcode}", style = MaterialTheme.typography.bodySmall)
                        Text("Price: $${product.price}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${product.quantity} units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.quantity < product.minQuantity) {
                            Text(
                                "LOW STOCK",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerView(
    scanState: InventoryViewModel.ScanResult,
    onBarcodeDetected: (String) -> Unit,
    onResetScan: () -> Unit,
    onCreateProduct: (String, String, Double, Int, Int) -> Unit,
    onUpdateQuantity: (ProductEntity, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    previewView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), BarcodeAnalyzer { barcode ->
                                    onBarcodeDetected(barcode)
                                })
                            }
                        
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay based on state
            if (scanState !is InventoryViewModel.ScanResult.Idle) {
                // Dim background
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                
                // Bottom Sheet Content
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            when (scanState) {
                                is InventoryViewModel.ScanResult.Found -> {
                                    val product = scanState.product
                                    Text("Product Found!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(product.name, style = MaterialTheme.typography.headlineSmall)
                                    Text("$${product.price} - Stock: ${product.quantity}")
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(onClick = { onUpdateQuantity(product, -1) }) { Text("-1") }
                                        Button(onClick = { onUpdateQuantity(product, 1) }) { Text("+1") }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = onResetScan, modifier = Modifier.fillMaxWidth()) { Text("Scan Next") }
                                }
                                is InventoryViewModel.ScanResult.NotFound -> {
                                    Text("Product Not Found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                    Text("Barcode: ${scanState.barcode}")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    var name by remember { mutableStateOf("") }
                                    var price by remember { mutableStateOf("") }
                                    var qty by remember { mutableStateOf("1") }
                                    
                                    OutlinedTextField(
                                        value = name, onValueChange = { name = it },
                                        label = { Text("Product Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = price, onValueChange = { price = it },
                                        label = { Text("Price") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onCreateProduct(scanState.barcode, name, price.toDoubleOrNull() ?: 0.0, qty.toIntOrNull() ?: 1, 5)
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                    ) {
                                        Text("Create Product")
                                    }
                                    TextButton(onClick = onResetScan, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        } else {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("Camera permission required")
             }
        }
    }
}
