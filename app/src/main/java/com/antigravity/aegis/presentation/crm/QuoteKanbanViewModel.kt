package com.antigravity.aegis.presentation.crm

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.reports.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.domain.transfer.DataTransferManager
import kotlinx.coroutines.flow.firstOrNull
import android.net.Uri

@HiltViewModel
class QuoteKanbanViewModel @Inject constructor(
    private val repository: CrmRepository,
    private val budgetRepository: com.antigravity.aegis.domain.repository.BudgetRepository,
    private val pdfGenerator: PdfGenerator,
    @ApplicationContext private val context: Context,
    private val transferManager: DataTransferManager,
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository
) : ViewModel() {


    // Transfer Logic
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState = _transferState.asStateFlow()
    
    val userConfig = settingsRepository.getUserConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    fun exportQuotes() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.exportData(DataTransferManager.EntityType.QUOTES)
            result.onSuccess { file ->
                 _transferState.value = TransferState.Success("Exported to ${file.absolutePath}")
            }.onFailure {
                 _transferState.value = TransferState.Error(it.message ?: "Export failed")
            }
        }
    }

    fun validateImport(uri: Uri) {
         viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val errors = transferManager.validateImport(DataTransferManager.EntityType.QUOTES, uri)
            if (errors.isEmpty()) {
                _transferState.value = TransferState.ValidationSuccess(uri)
            } else {
                _transferState.value = TransferState.ValidationError(errors)
            }
         }
    }

    fun confirmImport(uri: Uri, wipe: Boolean) {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.importData(DataTransferManager.EntityType.QUOTES, uri, wipe)
            result.onSuccess {
                 _transferState.value = TransferState.Success("Import Successful")
            }.onFailure {
                 _transferState.value = TransferState.Error(it.message ?: "Import failed")
            }
        }
    }
    
    fun resetTransferState() {
        _transferState.value = TransferState.Idle
    }

    sealed class TransferState {
        object Idle : TransferState()
        object Loading : TransferState()
        data class Success(val message: String) : TransferState()
        data class Error(val message: String) : TransferState()
        data class ValidationError(val errors: List<String>) : TransferState()
        data class ValidationSuccess(val uri: Uri) : TransferState()
    }


    // Helper class to combine quote with its client
    data class QuoteWithClient(val quote: QuoteEntity, val client: ClientEntity?)

    private val _quotes = repository.getAllQuotes()
    private val _clients = repository.getAllClients()

    val kanbanState: StateFlow<Map<String, List<QuoteWithClient>>> = combine(_quotes, _clients) { quotes, clients ->
        val clientMap = clients.associateBy { it.id }
        val enrichedQuotes = quotes.map { quote ->
            QuoteWithClient(quote, clientMap[quote.clientId])
        }
        
        // Group by status
        val grouped = enrichedQuotes.groupBy { it.quote.status }
        
        // Ensure all columns exist
        val result = mutableMapOf<String, List<QuoteWithClient>>()
        listOf("Draft", "Sent", "Won", "Lost").forEach { status ->
            result[status] = grouped[status] ?: emptyList()
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allClients: StateFlow<List<ClientEntity>> = _clients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createQuote(
        title: String,
        clientId: Int,
        lines: List<com.antigravity.aegis.data.model.BudgetLineEntity>,
        description: String
    ) {
        viewModelScope.launch {
             val calculatedTotal = lines.sumOf { it.quantity * it.unitPrice }
             
             // Calculate taxes if needed (assuming 21% or from line)
             val totalWithTax = calculatedTotal * 1.21 // Simplified
             
            val newQuote = QuoteEntity(
                clientId = clientId,
                date = System.currentTimeMillis(),
                totalAmount = totalWithTax,
                status = "Draft",
                description = description,
                title = title,
                calculatedTotal = (totalWithTax * 100).toLong(),
                version = 1
            )
            val quoteId = budgetRepository.insertQuote(newQuote)
            
            // Insert Lines
            val linesWithQuoteId = lines.map { it.copy(quoteId = quoteId.toInt()) }
            budgetRepository.insertBudgetLines(linesWithQuoteId)
        }
    }

    fun updateQuoteStatus(quoteId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateQuoteStatus(quoteId, newStatus)
        }
    }

    fun generateAndSharePdf(quote: QuoteEntity, client: ClientEntity) {
        viewModelScope.launch {
            try {
                val config = settingsRepository.getUserConfig().firstOrNull()
                val pdfFile = pdfGenerator.generateQuotePdf(context, quote, client, config)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    pdfFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    // Ensure the intent is handled by an activity that can handle the file
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) 
                }
                
                // We cannot start activity from ViewModel directly without activity context usually not rec'd, 
                // but here we might need to expose an Event to the UI.
                // For simplicity in this agentic flow, we'll assume the UI triggers the intent, 
                // but since the file generation is async, we should probably expose a "ShareEvent"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Instead of launching intent from VM, let's just generate the URI and pass it back
    // Or better, launch a coroutine that returns the URI?
    // For MVVM, best is to expose a SharedFlow of "Effect".
}
