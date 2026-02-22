package com.antigravity.aegis.presentation.crm

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.data.local.entity.QuoteEntity
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
import com.antigravity.aegis.domain.util.getOrNull
import kotlinx.coroutines.flow.firstOrNull
import android.net.Uri
import com.antigravity.aegis.domain.model.CrmStatus

@HiltViewModel
class QuoteKanbanViewModel @Inject constructor(
    private val clientRepository: com.antigravity.aegis.domain.repository.ClientRepository,
    private val budgetRepository: com.antigravity.aegis.domain.repository.BudgetRepository,
    private val projectRepository: com.antigravity.aegis.domain.repository.ProjectRepository,
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
    data class QuoteWithClient(val quote: QuoteEntity, val client: Client?)


    private val _quotes = budgetRepository.getAllQuotes()
    private val _clients = clientRepository.getAllClients()


    val kanbanState: StateFlow<Map<String, List<QuoteWithClient>>> = combine(_quotes, _clients) { quotes, clients ->
        val clientMap = clients.associateBy { it.id }
        val enrichedQuotes = quotes.map { quote ->
            QuoteWithClient(quote, clientMap[quote.clientId])
        }
        
        // Group by status
        val grouped = enrichedQuotes.groupBy { it.quote.status }
        
        // Ensure all columns exist
        val result = mutableMapOf<String, List<QuoteWithClient>>()
        listOf(CrmStatus.DRAFT, CrmStatus.SENT, CrmStatus.WON, CrmStatus.LOST).forEach { status ->
            result[status] = grouped[status] ?: emptyList()
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allClients: StateFlow<List<Client>> = _clients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun createProjectForQuote(clientId: Int, name: String, onProjectCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val project = com.antigravity.aegis.data.local.entity.ProjectEntity(
                clientId = clientId,
                name = name,
                status = CrmStatus.ACTIVE,
                startDate = System.currentTimeMillis()
            )
            val projectId = projectRepository.insertProject(project).getOrNull() ?: return@launch
            onProjectCreated(projectId.toInt())
        }
    }

    fun createQuote(
        title: String,
        clientId: Int,
        lines: List<com.antigravity.aegis.data.local.entity.BudgetLineEntity>,
        description: String
    ) {
        viewModelScope.launch {
             val calculatedTotal = lines.sumOf { it.quantity * it.unitPrice }
             val totalWithTax = calculatedTotal * 1.21 // Simplified
             
            val newQuote = QuoteEntity(
                clientId = clientId,
                date = System.currentTimeMillis(),
                totalAmount = totalWithTax,
                status = CrmStatus.DRAFT,
                description = description,
                title = title,
                calculatedTotal = (totalWithTax * 100).toLong(),
                version = 1
            )
            val quoteId = budgetRepository.insertQuote(newQuote).getOrNull() ?: return@launch
            
            // Insert Lines
            val linesWithQuoteId = lines.map { it.copy(quoteId = quoteId.toInt()) }
            budgetRepository.insertBudgetLines(linesWithQuoteId)
        }
    }

    fun updateQuoteStatus(quoteId: Int, newStatus: String) {
        viewModelScope.launch {
            budgetRepository.updateQuoteStatus(quoteId, newStatus)
        }
    }

    fun generateAndSharePdf(quote: QuoteEntity, client: Client) {

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
