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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.domain.transfer.DataTransferManager
import com.antigravity.aegis.domain.util.getOrNull
import com.antigravity.aegis.domain.util.onSuccess
import com.antigravity.aegis.domain.util.onError
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
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository,
    private val createProjectWithDraftQuoteUseCase: com.antigravity.aegis.domain.usecase.project.CreateProjectWithDraftQuoteUseCase,
    private val generateQuotePdfUseCase: com.antigravity.aegis.domain.usecase.crm.GenerateQuotePdfUseCase,
    private val deleteProjectWithQuotesUseCase: com.antigravity.aegis.domain.usecase.project.DeleteProjectWithQuotesUseCase
) : ViewModel() {

    private val _pdfShareEvent = kotlinx.coroutines.flow.MutableSharedFlow<Uri>()
    val pdfShareEvent = _pdfShareEvent.asSharedFlow()


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
            createProjectWithDraftQuoteUseCase(
                name = name,
                description = null,
                clientId = clientId,
                category = null,
                startDate = System.currentTimeMillis(),
                endDate = null,
                subProjects = emptyList()
            ).onSuccess { projectId ->
                onProjectCreated(projectId.toInt())
            }.onError { e ->
                android.util.Log.e("QuoteKanbanViewModel", "Error al crear proyecto para quote: ${e.message}")
            }
        }
    }

    fun createQuote(
        title: String,
        clientId: Int,
        lines: List<com.antigravity.aegis.data.local.entity.BudgetLineEntity>,
        description: String
    ) {
        viewModelScope.launch {
            // Mapear BudgetLineEntity a SubProjectInput para que el usecase cree tanto subproyectos como líneas
            val subProjectsInput = lines.map { line ->
                com.antigravity.aegis.domain.usecase.project.SubProjectInput(
                    name = line.description,
                    materials = null, // Extraer si estuviese parseado, por ahora null
                    price = line.unitPrice,
                    estimatedTime = null,
                    estimatedTimeUnit = null
                )
            }

            createProjectWithDraftQuoteUseCase(
                name = title,
                description = description,
                clientId = clientId,
                category = null,
                startDate = System.currentTimeMillis(),
                endDate = null,
                subProjects = subProjectsInput
            ).onSuccess { projectId ->
                // Éxito: Se ha creado el Proyecto, los subproyectos y el Presupuesto borrador sincronizado.
                android.util.Log.d("QuoteKanbanViewModel", "Proyecto/Presupuesto creado con el ID de proyecto: $projectId")
            }.onError { e ->
                android.util.Log.e("QuoteKanbanViewModel", "Error al crear proyecto desde Kanban: ${e.message}")
            }
        }
    }

    fun updateQuoteStatus(quoteId: Int, newStatus: String) {
        viewModelScope.launch {
            budgetRepository.updateQuoteStatus(quoteId, newStatus)
        }
    }

    fun generateAndSharePdf(quoteId: Int) {
        viewModelScope.launch {
            val result = generateQuotePdfUseCase(quoteId)
            result.onSuccess { file ->
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    _pdfShareEvent.emit(uri)
                } catch (e: Exception) {
                    android.util.Log.e("QuoteKanbanViewModel", "Error obteniendo URI", e)
                }
            }.onError { e ->
                android.util.Log.e("QuoteKanbanViewModel", "Error generando PDF", e.exception)
            }
        }
    }
    
    // Instead of launching intent from VM, let's just generate the URI and pass it back
    // Or better, launch a coroutine that returns the URI?
    // For MVVM, best is to expose a SharedFlow of "Effect".

    fun deleteQuoteAndProject(quote: QuoteEntity) {
        viewModelScope.launch {
            if (quote.projectId != null) {
                deleteProjectWithQuotesUseCase(quote.projectId)
            } else {
                budgetRepository.deleteBudgetLines(quote.id)
                budgetRepository.deleteQuote(quote.id)
            }
        }
    }

    fun scheduleQuoteReminder(quoteId: Int, title: String, days: Int, times: Int) {
        com.antigravity.aegis.data.worker.QuoteFollowUpWorker.scheduleNextReminder(context, quoteId, title, days, times)
    }

    fun cancelQuoteReminder(quoteId: Int) {
        androidx.work.WorkManager.getInstance(context).cancelUniqueWork("quote_reminder_$quoteId")
    }
}
