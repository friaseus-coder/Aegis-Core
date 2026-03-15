package com.antigravity.aegis.presentation.crm

import com.antigravity.aegis.R
import com.antigravity.aegis.domain.model.CrmStatus

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.domain.model.Address
import com.antigravity.aegis.domain.model.ClientCategory
import com.antigravity.aegis.domain.repository.ClientRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.repository.DocumentRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ExpenseRepository
import com.antigravity.aegis.domain.reports.PdfGenerator
import com.antigravity.aegis.domain.util.onError
import com.antigravity.aegis.domain.util.onSuccess
import com.antigravity.aegis.domain.util.getOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import com.antigravity.aegis.domain.transfer.DataTransferManager
import com.antigravity.aegis.domain.util.Result
import com.antigravity.aegis.data.repository.AttachmentRepository
import com.antigravity.aegis.data.local.entity.DocumentEntity
import android.provider.OpenableColumns
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val taskRepository: TaskRepository,
    private val documentRepository: DocumentRepository,
    private val projectRepository: ProjectRepository,
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val getProjectRealProfitUseCase: com.antigravity.aegis.domain.usecase.GetProjectRealProfitUseCase,
    private val pdfGenerator: PdfGenerator,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val transferManager: DataTransferManager,
    private val attachmentRepository: AttachmentRepository,
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository,
    private val createQuoteFromProjectUseCase: com.antigravity.aegis.domain.usecase.project.CreateQuoteFromProjectUseCase,
    private val saveProjectAsTemplateUseCase: com.antigravity.aegis.domain.usecase.project.SaveProjectAsTemplateUseCase,
    private val createProjectFromTemplateUseCase: com.antigravity.aegis.domain.usecase.project.CreateProjectFromTemplateUseCase,
    private val exportTemplateUseCase: com.antigravity.aegis.domain.usecase.project.ExportTemplateUseCase,
    private val importTemplateUseCase: com.antigravity.aegis.domain.usecase.project.ImportTemplateUseCase,
    // Nuevos casos de uso: sincronización Proyectos ↔ CRM
    private val createProjectWithDraftQuoteUseCase: com.antigravity.aegis.domain.usecase.project.CreateProjectWithDraftQuoteUseCase,
    private val generateQuotePdfUseCase: com.antigravity.aegis.domain.usecase.crm.GenerateQuotePdfUseCase,
    private val deleteProjectWithQuotesUseCase: com.antigravity.aegis.domain.usecase.project.DeleteProjectWithQuotesUseCase
) : ViewModel() {


    // --- Filter State ---
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<String?>(null) // "Todos" (null), "Particular", "Empresa"

    // --- Clients ---
    // We combine the search query and the filter type.
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allClients: StateFlow<List<Client>> = combine(

        _searchQuery,
        _filterType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        val sourceFlow = if (query.length >= 3) {
            clientRepository.searchClients(query)
        } else {
            clientRepository.getAllClients()
        }

        
        combine(sourceFlow, kotlinx.coroutines.flow.flowOf(type)) { clients, filterType ->
             if (filterType == null) {
                 clients
             } else {
                 val targetType = when(filterType) {
                     "Particular" -> ClientType.PARTICULAR
                     "Empresa" -> ClientType.EMPRESA
                     else -> null
                 }
                 if (targetType != null) {
                     clients.filter { client -> client.tipoCliente == targetType }
                 } else {
                     clients
                 }
             }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterType(type: String?) { _filterType.value = type }

    // --- Project Filters ---
    private val _selectedProjectStatuses = MutableStateFlow(setOf(CrmStatus.ACTIVE, CrmStatus.DRAFT, CrmStatus.SENT))
    val selectedProjectStatuses = _selectedProjectStatuses.asStateFlow()

    fun toggleProjectStatusFilter(status: String) {
        _selectedProjectStatuses.value = if (_selectedProjectStatuses.value.contains(status)) {
            _selectedProjectStatuses.value - status
        } else {
            _selectedProjectStatuses.value + status
        }
    }

    // --- Projects ---
    val activeProjects: StateFlow<List<ProjectEntity>> = projectRepository.getActiveProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeRootProjects: StateFlow<List<ProjectEntity>> = combine(
        projectRepository.getActiveRootProjects(), // Idealmente esto ahora devuelve todos los que no esten archivados si cambiamos la query, o podemos filtrar en memoria.
        _selectedProjectStatuses
    ) { projects, selectedStatuses ->
        projects.filter { selectedStatuses.contains(it.status) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selection State ---
    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient


    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject

    val currencySymbol = settingsRepository.getUserConfig()
        .map { config ->
            com.antigravity.aegis.domain.util.CurrencyUtils.getCurrencySymbol(config?.currency ?: "EUR")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

    // --- Derived State for Selected Client's Projects ---
    private val _clientProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val clientProjects: StateFlow<List<ProjectEntity>> = _clientProjects

    // --- Derived State for Selected Client's Documents ---
    private val _clientDocuments = MutableStateFlow<List<DocumentEntity>>(emptyList())
    val clientDocuments: StateFlow<List<DocumentEntity>> = _clientDocuments

    // --- Derived State for Selected Project's Tasks ---
    private val _projectTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val projectTasks: StateFlow<List<TaskEntity>> = _projectTasks

    // --- Budgets ---
    private val _projectBudgets = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val projectBudgets: StateFlow<List<QuoteEntity>> = _projectBudgets

    // Transfer Logic
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState = _transferState.asStateFlow()

    fun exportClients() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.exportData(DataTransferManager.EntityType.CLIENTS)
            result.onSuccess { file ->
                 _transferState.value = TransferState.Success(resId = R.string.crm_export_client_success, arg = file.absolutePath)
            }.onError {
                 _transferState.value = TransferState.Error(resId = R.string.general_unknown_error)
            }
        }
    }

    fun validateImport(uri: android.net.Uri) {
         viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val errors = transferManager.validateImport(DataTransferManager.EntityType.CLIENTS, uri)
            if (errors.isEmpty()) {
                _transferState.value = TransferState.ValidationSuccess(uri)
            } else {
                _transferState.value = TransferState.ValidationError(errors)
            }
         }
    }

    fun confirmImport(uri: android.net.Uri, wipe: Boolean) {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            
            val backupName = if (wipe) {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val dateStr = sdf.format(Date())
                context.getString(R.string.crm_clients_backup_name_prefix) + dateStr
            } else null

            val result = transferManager.importData(
                type = DataTransferManager.EntityType.CLIENTS, 
                uri = uri, 
                wipeExisting = wipe,
                backupName = backupName
            )
            
            result.onSuccess { backupGenerated ->
                if (backupGenerated != null) {
                    _transferState.value = TransferState.Success(
                        resId = R.string.crm_clients_import_success_with_backup,
                        arg = backupGenerated
                    )
                } else {
                    _transferState.value = TransferState.Success(resId = R.string.crm_import_client_success)
                }
            }.onError {
                 _transferState.value = TransferState.Error(resId = R.string.general_unknown_error)
            }
        }
    }

    fun downloadTemplate() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.generateTemplate(DataTransferManager.EntityType.CLIENTS)
            result.onSuccess { file ->
                _transferState.value = TransferState.Success(resId = R.string.crm_template_export_success, arg = file.absolutePath)
            }.onError {
                _transferState.value = TransferState.Error(resId = R.string.general_unknown_error)
            }
        }
    }
    
    fun resetTransferState() {
        _transferState.value = TransferState.Idle
    }

    sealed class TransferState {
        object Idle : TransferState()
        object Loading : TransferState()
        data class Success(val message: String? = null, val resId: Int? = null, val arg: String? = null) : TransferState()
        data class Error(val message: String? = null, val resId: Int? = null, val arg: String? = null) : TransferState()
        data class ValidationError(val errors: List<String>) : TransferState()
        data class ValidationSuccess(val uri: android.net.Uri) : TransferState()
    }

    // Create new objects
    fun createClient(
        firstName: String,
        lastName: String,
        tipoCliente: String,
        razonSocial: String?,
        nifCif: String?,
        personaContacto: String?,
        phone: String?,
        email: String?,
        // Address
        calle: String?,
        numero: String?,
        piso: String?,
        poblacion: String?,
        codigoPostal: String?,
        // Notes
        notas: String?
    ) {
        viewModelScope.launch {
            val client = Client(
                firstName = firstName,
                lastName = lastName,
                tipoCliente = if (tipoCliente == "Empresa") ClientType.EMPRESA else ClientType.PARTICULAR,
                razonSocial = razonSocial,
                nifCif = nifCif,
                personaContacto = personaContacto,
                phone = phone,
                email = email,
                address = Address(
                    calle = calle,
                    numero = numero,
                    piso = piso,
                    poblacion = poblacion,
                    codigoPostal = codigoPostal
                ),
                notas = notas,
                categoria = ClientCategory.POTENTIAL
            )
            clientRepository.insertClient(client)

        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            clientRepository.insertClient(client) // Room Insert(OnConflictStrategy.REPLACE) acts as update
        }
    }


    fun uploadDocument(uri: Uri) {
        val client = _selectedClient.value ?: return
        viewModelScope.launch {
            // 1. Get Metadata
            var fileName = "unknown"
            var size = 0L
            var mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }

            // 2. Encrypt and Save Local
            // We use a unique name internally: client_{id}_{timestamp}_{filename}
            val internalName = "client_${client.id}_${System.currentTimeMillis()}_$fileName"
            
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    attachmentRepository.saveAttachmentEncrypted(internalName, input)
                }
                
                // 3. Save Metadata to DB
                val doc = DocumentEntity(
                    clientId = client.id,
                    fileName = internalName,
                    originalName = fileName,
                    mimeType = mimeType,
                    size = size,
                    dateAdded = System.currentTimeMillis()
                )
                documentRepository.addDocument(doc)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    fun exportDocument(document: DocumentEntity, destinationUri: Uri) {
         viewModelScope.launch {
             attachmentRepository.exportAttachmentToUri(document.fileName, destinationUri)
         }
    }
    
    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            documentRepository.deleteDocument(document)
            attachmentRepository.deleteAttachment(document.fileName)
        }
    }

    fun createProject(clientId: Int, name: String, status: String, startDate: Long, endDate: Long?, category: String? = null) {
        viewModelScope.launch {
            val validStatus = if (CrmStatus.isValid(status)) status else CrmStatus.DRAFT
            
            createProjectWithDraftQuoteUseCase(
                name = name,
                description = null,
                clientId = clientId,
                category = category,
                startDate = startDate,
                endDate = endDate,
                subProjects = emptyList()
            ).onSuccess { projectId ->
                // Ensure the status matches what was requested if it's not ACTIVE
                if (validStatus != CrmStatus.ACTIVE) {
                    projectRepository.updateProjectStatus(projectId.toInt(), validStatus)
                }
            }.onError { e -> 
                android.util.Log.e("CrmViewModel", "Error al crear proyecto: ${e.message}") 
            }
        }
    }

    fun createTask(projectId: Int, description: String, estimatedDuration: Long? = null) {
        viewModelScope.launch {
            // TaskEntity uses 'description' as primary content? Or 'title'? 
            // In TaskEntity: val description: String
            // Previous error said "No value passed for parameter 'title'".
            // Let's check TaskEntity. If it requires title, we must provide it.
            // Assuming description maps to description, maybe title is required.
            // I'll assume Title is "Task" or same as description for now if required.
            // But wait, I need to see TaskEntity to be sure. 
            // Better: use named arguments to be safe or check entity. 
            // Error: No value passed for parameter 'title'.
            // So TaskEntity has (..., title, description, ...).
            val task = TaskEntity(
                projectId = projectId, 
                title = description, 
                description = "",
                estimatedDuration = estimatedDuration
            ) 
            taskRepository.insertTask(task)
        }
    }
    
    fun updateTaskStatus(task: TaskEntity, isCompleted: Boolean) {
         viewModelScope.launch {
            taskRepository.updateTaskStatus(task.id, isCompleted)
        }
    }
    // Navigation and Selection
    fun selectClient(clientId: Int) {
        viewModelScope.launch {
            _selectedClient.value = clientRepository.getClientById(clientId).firstOrNull()
            
            launch {
                projectRepository.getProjectsByClient(clientId).collect { projects ->
                    _clientProjects.value = projects
                }
            }
            
            launch {
                documentRepository.getDocumentsForClient(clientId).collect { docs ->
                    _clientDocuments.value = docs
                }
            }

        }
    }

    // --- Derived State for Selected Project's Expenses ---
    private val _projectExpenses = MutableStateFlow<List<com.antigravity.aegis.data.local.entity.ExpenseEntity>>(emptyList())
    val projectExpenses: StateFlow<List<com.antigravity.aegis.data.local.entity.ExpenseEntity>> = _projectExpenses

    data class FinancialSummary(
        val totalIncome: Double = 0.0,
        val directExpenses: Double = 0.0,
        val allocatedGeneralExpenses: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val netProfit: Double = 0.0,
        val margin: Double = 0.0
    )
    
    // Switch map on selectedProject to fetch profit
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val financialSummary: StateFlow<FinancialSummary> = _selectedProject.flatMapLatest { project ->
        if (project != null) {
            // We use a flow builder to emit values, refreshing periodically or based on triggers
            // But UseCase is suspend. We can wrap it in a flow that re-triggers on signal.
            // For now, simple flow calling usecase once. Ideally, we should observe changes.
            // Since UseCase pulls from DB, we might want to observe DB changes.
            // But UseCase uses multiple repos. 
            // Better approach: Trigger recalculation when relevant flows emit.
            combine(_projectBudgets, _projectExpenses) { _, _: Any -> 
                // Recalculate
                try {
                   val profitability = getProjectRealProfitUseCase(project.id)
                   FinancialSummary(
                       totalIncome = profitability?.totalIncome ?: 0.0,
                       directExpenses = profitability?.directExpenses ?: 0.0,
                       allocatedGeneralExpenses = profitability?.allocatedGeneralExpenses ?: 0.0,
                       totalExpenses = profitability?.totalExpenses ?: 0.0,
                       netProfit = profitability?.netProfit ?: 0.0,
                       margin = profitability?.profitMargin ?: 0.0
                   )
                } catch (e: Exception) {
                    FinancialSummary()
                }
            }
        } else {
             kotlinx.coroutines.flow.flowOf(FinancialSummary())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())
    
    // Archived Projects
    val archivedProjects: StateFlow<List<ProjectEntity>> = projectRepository.getProjectsByStatus(CrmStatus.ARCHIVED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reactivateProject(projectId: Int) {
        viewModelScope.launch {
            projectRepository.updateProjectStatus(projectId, CrmStatus.ACTIVE)
        }
    }

    fun selectProject(projectId: Int) {
         viewModelScope.launch {
            _selectedProject.value = projectRepository.getProjectById(projectId)
            // Fetch tasks
            launch {
                taskRepository.getTasksByProject(projectId).collect { tasks ->
                    _projectTasks.value = tasks
                }
            }

            // Fetch budgets
            launch {
                budgetRepository.getQuotesByProject(projectId).collect { budgets ->
                    _projectBudgets.value = budgets
                }
            }
            // Fetch expenses
            launch {
                expenseRepository.getExpensesByProject(projectId).collect { expenses ->
                    _projectExpenses.value = expenses
                }
            }
            // Fetch subprojects
            launch {
                projectRepository.getSubProjects(projectId).collect { subs ->
                    _subProjects.value = subs
                }
            }
        }
    }
    
    fun clearSelection() {
        _selectedClient.value = null
        _selectedProject.value = null
        _subProjects.value = emptyList()
    }

    // --- SubProjects & Conversions ---
    private val _subProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val subProjects: StateFlow<List<ProjectEntity>> = _subProjects

    fun createSubProject(
        parentProjectId: Int, 
        name: String, 
        startDate: Long,
        materials: String? = null,
        price: Double? = null,
        estimatedTime: Double? = null,
        estimatedTimeUnit: String? = null
    ) {
        viewModelScope.launch {
            val parent = projectRepository.getProjectById(parentProjectId) ?: return@launch
            val subProject = ProjectEntity(
                clientId = parent.clientId,
                parentProjectId = parentProjectId,
                name = name,
                status = CrmStatus.ACTIVE,
                startDate = startDate,
                materials = materials,
                price = price,
                estimatedTime = estimatedTime,
                estimatedTimeUnit = estimatedTimeUnit
            )
            projectRepository.insertProject(subProject)
                .onSuccess {
                    // Sincronizar subproyecto como nueva línea en el presupuesto DRAFT
                    val quote = budgetRepository.getQuoteByProjectId(parentProjectId)
                    if (quote != null && quote.status == CrmStatus.DRAFT) {
                        val linePrice = price ?: 0.0
                        val tax = 0.21
                        val totalLineWithTax = linePrice * (1 + tax)
                        val newTotal = quote.totalAmount + totalLineWithTax
                        
                        budgetRepository.updateQuote(quote.copy(
                            totalAmount = newTotal,
                            calculatedTotal = (newTotal * 100).toLong()
                        ))
                        
                        val line = com.antigravity.aegis.data.local.entity.BudgetLineEntity(
                            quoteId = quote.id,
                            description = buildString {
                                append(name)
                                if (!materials.isNullOrBlank()) append(" | Mat: $materials")
                                if (estimatedTime != null && estimatedTimeUnit != null) {
                                    append(" | $estimatedTime $estimatedTimeUnit")
                                }
                            },
                            quantity = 1.0,
                            unitPrice = linePrice,
                            taxRate = tax
                        )
                        budgetRepository.insertBudgetLine(line)
                    }
                }
                .onError { android.util.Log.e("CrmViewModel", "Error al crear subproyecto: ${it.message}") }
        }
    }

    fun deleteSubProject(subProjectId: Int) {
        viewModelScope.launch {
            val entity = projectRepository.getProjectById(subProjectId) ?: return@launch
            projectRepository.deleteProject(entity)
        }
    }

    fun updateSubProject(
        subProjectId: Int,
        name: String,
        materials: String? = null,
        price: Double? = null,
        estimatedTime: Double? = null,
        estimatedTimeUnit: String? = null
    ) {
        viewModelScope.launch {
            val existing = projectRepository.getProjectById(subProjectId) ?: return@launch
            val updated = existing.copy(
                name = name,
                materials = materials,
                price = price,
                estimatedTime = estimatedTime,
                estimatedTimeUnit = estimatedTimeUnit
            )
            projectRepository.updateProject(updated)
        }
    }

    fun createQuoteFromProject(projectId: Int, onResult: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val quoteId = createQuoteFromProjectUseCase(projectId)
                onResult(quoteId)
            } catch (e: Exception) {
                android.util.Log.e("CrmViewModel", "Error creating quote from project", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    // ─── Nuevas funciones: Sincronización Proyectos ↔ CRM ─────────────────────

    /** SharedFlow que emite el URI del PDF generado para que la UI lo comparta */
    private val _pdfShareEvent = MutableSharedFlow<Uri>()
    val pdfShareEvent = _pdfShareEvent.asSharedFlow()

    /**
     * Crea un proyecto padre, inserta sus subproyectos y genera automáticamente
     * un Presupuesto DRAFT en el CRM vinculado al proyecto.
     */
    fun createProjectWithDraftQuote(
        name: String,
        description: String?,
        clientId: Int?,
        category: String?,
        startDate: Long,
        endDate: Long?,
        subProjects: List<com.antigravity.aegis.domain.usecase.project.SubProjectInput>,
        onResult: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = createProjectWithDraftQuoteUseCase(
                name = name,
                description = description,
                clientId = clientId,
                category = category,
                startDate = startDate,
                endDate = endDate,
                subProjects = subProjects
            )
            result.onSuccess { projectId -> onResult(projectId) }
                  .onError { e -> onError(e.message ?: "Error al crear proyecto") }
        }
    }

    /**
     * Genera el PDF de un presupuesto y emite el URI a través de [pdfShareEvent]
     * para que la UI lo comparta con Intent.ACTION_SEND.
     */
    fun generateAndShareQuotePdf(quoteId: Int) {
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
                    android.util.Log.e("CrmViewModel", "Error obteniendo URI del PDF", e)
                }
            }.onError { err ->
                android.util.Log.e("CrmViewModel", "Error generando PDF", err.exception)
            }
        }
    }

    /**
     * Borra un proyecto y todas sus entidades CRM asociadas:
     * quotes, budget lines, subproyectos y el proyecto padre.
     */
    fun deleteProjectWithQuotes(
        projectId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = deleteProjectWithQuotesUseCase(projectId)
            result.onSuccess { onSuccess() }
                  .onError { e -> onError(e.message ?: "Error al borrar el proyecto") }
        }
    }

    // --- Template Management ---
    val templates: StateFlow<List<ProjectEntity>> = projectRepository.getTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templateCategories: StateFlow<List<String>> = projectRepository.getAllCategories()
        .map { dbCategories -> 
            val localizedDefaults = listOf(
                context.getString(R.string.crm_template_cat_renovation),
                context.getString(R.string.crm_template_cat_marketing),
                context.getString(R.string.crm_template_cat_systems),
                context.getString(R.string.crm_template_cat_sales),
                context.getString(R.string.crm_template_cat_admin),
                context.getString(R.string.filter_others)
            )
            (dbCategories + localizedDefaults).distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveAsTemplate(projectId: Int, templateName: String, category: String? = null) {
        viewModelScope.launch {
            saveProjectAsTemplateUseCase(projectId, templateName, category)
        }
    }

    fun createProjectFromTemplate(templateId: Int, clientId: Int, name: String, startDate: Long, endDate: Long?) {
        viewModelScope.launch {
            createProjectFromTemplateUseCase(templateId, clientId, name, startDate, endDate)
        }
    }

    fun exportTemplate(templateId: Int, uri: Uri) {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            exportTemplateUseCase(templateId, uri)
                .onSuccess { _transferState.value = TransferState.Success(resId = R.string.crm_template_export_success) }
                .onError { _transferState.value = TransferState.Error(resId = R.string.general_unknown_error) }
        }
    }

    fun importTemplate(uri: Uri) {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            importTemplateUseCase(uri)
                .onSuccess { _transferState.value = TransferState.Success(resId = R.string.crm_template_import_success) }
                .onError { _transferState.value = TransferState.Error(resId = R.string.general_unknown_error) }
        }
    }

    fun shareSampleTemplate() {
        viewModelScope.launch {
            try {
                // Copiar el asset a un fichero temporal en cache y compartirlo
                val sampleJson = context.assets.open("sample_template.json").bufferedReader().readText()
                val cacheFile = java.io.File(context.cacheDir, "sample_template.json")
                cacheFile.writeText(sampleJson)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    cacheFile
                )

                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Guardar plantilla de muestra en...").apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                android.util.Log.e("CrmViewModel", "Error sharing sample template", e)
                _transferState.value = TransferState.Error(message = "Error: ${e.message}")
            }
        }
    }
}
