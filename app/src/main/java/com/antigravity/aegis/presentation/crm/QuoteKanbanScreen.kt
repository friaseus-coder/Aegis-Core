package com.antigravity.aegis.presentation.crm

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.domain.reports.PdfGenerator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.util.Locale

import com.antigravity.aegis.presentation.common.ImportConfirmationDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteKanbanScreen(
    viewModel: QuoteKanbanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCreateQuote: () -> Unit
) {
    val kanbanState by viewModel.kanbanState.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
        is QuoteKanbanViewModel.TransferState.Success -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetTransferState()
        }
        is QuoteKanbanViewModel.TransferState.Error -> {
            Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
             viewModel.resetTransferState()
        }
        is QuoteKanbanViewModel.TransferState.ValidationError -> {
             AlertDialog(
                onDismissRequest = { viewModel.resetTransferState() },
                title = { Text("Import Errors") },
                text = { Text(state.errors.joinToString("\n")) },
                confirmButton = { TextButton(onClick = { viewModel.resetTransferState() }) { Text("OK") } }
            )
        }
        is QuoteKanbanViewModel.TransferState.ValidationSuccess -> {
            ImportConfirmationDialog(
                onConfirm = { wipe -> viewModel.confirmImport(state.uri, wipe) },
                onCancel = { viewModel.resetTransferState() }
            )
        }
        else -> {}
    }
    
    // Simple statuses
    val statuses = listOf("Draft", "Sent", "Won", "Lost")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos (Kanban)") },
                navigationIcon = {
                     IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportQuotes() }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Export CSV")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Import CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateQuote) {
                Icon(Icons.Default.Add, contentDescription = "New Quote")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Title removed since TopBar has it
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(statuses) { status ->
                KanbanColumn(
                    status = status,
                    quotes = kanbanState[status] ?: emptyList(),
                    onMoveQuote = { quoteId, newStatus ->
                        viewModel.updateQuoteStatus(quoteId, newStatus)
                    },
                    onShareQuote = { quote, client ->
                        // Generate and Share
                        val pdfGenerator = PdfGenerator() // Ideally injected, but for quick context action:
                        // Actually, better to trigger a VM function that returns the file or URI
                        // But for direct context usage in Composable (simpler for now):
                        scope.launch {
                             val pdfFile = pdfGenerator.generateQuotePdf(context, quote, client)
                             val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Quote"))
                        }
                    }
                )
            }
        }
    } // Column
    } // Scaffold
} // Function

@Composable
fun KanbanColumn(
    status: String,
    quotes: List<QuoteKanbanViewModel.QuoteWithClient>,
    onMoveQuote: (Int, String) -> Unit,
    onShareQuote: (QuoteEntity, ClientEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Badge { Text(quotes.size.toString()) }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(quotes) { item ->
                QuoteCard(
                    quote = item.quote,
                    client = item.client,
                    currentStatus = status,
                    onMoveTo = { newStatus -> onMoveQuote(item.quote.id, newStatus) },
                    onShare = { 
                        if (item.client != null) {
                            onShareQuote(item.quote, item.client) 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuoteCard(
    quote: QuoteEntity,
    client: ClientEntity?,
    currentStatus: String,
    onMoveTo: (String) -> Unit,
    onShare: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (client != null) {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share PDF") },
                            onClick = {
                                showMenu = false
                                onShare()
                            },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                        )
                        HorizontalDivider()
                        Text("Move to:", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall)
                        listOf("Draft", "Sent", "Won", "Lost").filter { it != currentStatus }.forEach { targetStatus ->
                            DropdownMenuItem(
                                text = { Text(targetStatus) },
                                onClick = {
                                    showMenu = false
                                    onMoveTo(targetStatus)
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val formatter = NumberFormat.getCurrencyInstance(Locale.US) // Or default
            Text(
                text = formatter.format(quote.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(quote.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
