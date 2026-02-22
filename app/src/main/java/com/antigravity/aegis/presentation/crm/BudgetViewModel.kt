package com.antigravity.aegis.presentation.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.domain.util.getOrNull

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val clientRepository: com.antigravity.aegis.domain.repository.ClientRepository,
    private val pdfGenerator: com.antigravity.aegis.domain.services.PdfGeneratorService
) : ViewModel() {

    private val _budgetState = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val budgetState: StateFlow<BudgetState> = _budgetState
    
    private val _pdfFile = MutableStateFlow<java.io.File?>(null)
    val pdfFile: StateFlow<java.io.File?> = _pdfFile

    private val _lines = MutableStateFlow<List<BudgetLineEntity>>(emptyList())
    val lines: StateFlow<List<BudgetLineEntity>> = _lines

    // Temporary state for new/editing budget
    private val _currentQuote = MutableStateFlow<QuoteEntity?>(null)
    val currentQuote: StateFlow<QuoteEntity?> = _currentQuote

    // ... (initNewBudget, loadBudget, updateQuoteDetails, addLine, removeLine, calculateTotal unchanged) ...

    fun initNewBudget(projectId: Int) {
        viewModelScope.launch {
            val project = projectRepository.getProjectById(projectId)
            if (project != null) {
                _currentQuote.value = QuoteEntity(
                    clientId = project.clientId ?: 0,
                    projectId = project.id,
                    date = System.currentTimeMillis(),
                    totalAmount = 0.0,
                    status = CrmStatus.DRAFT,
                    description = "",
                    title = "Presupuesto para ${project.name}"
                )
                _lines.value = emptyList()
                _budgetState.value = BudgetState.Editing
            } else {
                _budgetState.value = BudgetState.Error("Proyecto no encontrado")
            }
        }
    }

    fun loadBudget(quoteId: Int) {
        viewModelScope.launch {
            _budgetState.value = BudgetState.Loading
            val quote = budgetRepository.getQuoteById(quoteId)
            if (quote != null) {
                _currentQuote.value = quote
                budgetRepository.getBudgetLines(quoteId).collect {
                    _lines.value = it
                    calculateTotal()
                }
                _budgetState.value = BudgetState.Editing
            } else {
                _budgetState.value = BudgetState.Error("Presupuesto no encontrado")
            }
        }
    }

    fun updateQuoteDetails(title: String, description: String) {
        _currentQuote.value = _currentQuote.value?.copy(title = title, description = description)
    }

    fun addLine(description: String, quantity: Double, unitPrice: Double, taxRate: Double) {
        val newLine = BudgetLineEntity(
            quoteId = _currentQuote.value?.id ?: 0, 
            description = description,
            quantity = quantity,
            unitPrice = unitPrice,
            taxRate = taxRate
        )
        _lines.value = _lines.value + newLine
        calculateTotal()
    }
    
    fun removeLine(line: BudgetLineEntity) {
        _lines.value = _lines.value - line
        calculateTotal()
    }

    private fun calculateTotal() {
        val total = _lines.value.sumOf { (it.quantity * it.unitPrice) * (1 + it.taxRate) }
        _currentQuote.value = _currentQuote.value?.copy(totalAmount = total)
    }


    fun saveBudget() {
        viewModelScope.launch {
            val quote = _currentQuote.value ?: return@launch

            _budgetState.value = BudgetState.Loading

            try {
                val quoteId = withContext(Dispatchers.IO) {
                    budgetRepository.saveQuoteWithLines(quote, _lines.value).getOrNull()
                        ?: throw Exception("Error al guardar el presupuesto en la base de datos")
                }

                // --- AUTOGENERATE SUBPROJECTS FROM QUOTE LINES ---
                val currentLines = _lines.value
                val rootProjectId = quote.projectId
                if (rootProjectId != null) {
                    val existingSubProjects = projectRepository.getSubProjectsSync(rootProjectId)
                    val existingNames = existingSubProjects.map { it.name }.toSet()

                    currentLines.forEach { line ->
                        if (!existingNames.contains(line.description)) {
                            val subProject = com.antigravity.aegis.data.local.entity.ProjectEntity(
                                clientId = quote.clientId,
                                parentProjectId = rootProjectId,
                                name = line.description,
                                status = CrmStatus.ACTIVE,
                                startDate = System.currentTimeMillis(),
                                price = line.quantity * line.unitPrice
                            )
                            projectRepository.insertProject(subProject)
                                .getOrNull() // Si falla, simplemente no crea el subproyecto
                        }
                    }
                }

                _budgetState.value = BudgetState.Saved

                if (quote.id == 0) {
                    _currentQuote.value = quote.copy(id = quoteId.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _budgetState.value = BudgetState.Error("Error saving budget: ${e.message}")
            }
        }
    }
    
    fun generatePdf() {
        viewModelScope.launch {
            val quote = _currentQuote.value ?: return@launch
            if (quote.id == 0) {
                 _budgetState.value = BudgetState.Error("Save budget before generating PDF")
                 return@launch
            }
            
            val client = clientRepository.getClientById(quote.clientId).firstOrNull()
            
            val file = pdfGenerator.generateQuotePdf(quote, _lines.value, client)

            
            // Log Event
            budgetRepository.insertBudgetLog(
                com.antigravity.aegis.data.local.entity.BudgetLogEntity(
                    quoteId = quote.id,
                    timestamp = System.currentTimeMillis(),
                    action = "PDF Generated",
                    messageTemplateUsed = "PDF shared/viewed by user."
                )
            )
            
            _pdfFile.value = file
        }
    }
    
    fun clearPdfFile() {
        _pdfFile.value = null
    }

    sealed class BudgetState {
        object Loading : BudgetState()
        object Editing : BudgetState()
        object Saved : BudgetState()
        data class Error(val message: String) : BudgetState()
    }
}
