package com.antigravity.aegis.presentation.crm

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ExpenseRepository
import com.antigravity.aegis.domain.reports.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import com.antigravity.aegis.domain.transfer.DataTransferManager
import com.antigravity.aegis.data.repository.AttachmentRepository
import com.antigravity.aegis.data.local.entity.DocumentEntity
import android.provider.OpenableColumns
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val repository: CrmRepository,
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
    private val importTemplateUseCase: com.antigravity.aegis.domain.usecase.project.ImportTemplateUseCase
) : ViewModel() {


    // --- Filter State ---
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<String?>(null) // "Todos" (null), "Particular", "Empresa"

    // --- Clients ---
    // We combine the search query and the filter type.
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allClients: StateFlow<List<ClientEntity>> = combine(
        _searchQuery,
        _filterType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        val sourceFlow = if (query.length >= 3) {
            repository.searchClients(query)
        } else {
            repository.getAllClients()
        }
        
        combine(sourceFlow, kotlinx.coroutines.flow.flowOf(type)) { clients, filterType ->
             if (filterType == null || filterType == "Todos") {
                 clients
             } else {
                 clients.filter { client -> client.tipoCliente == filterType }
             }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterType(type: String?) { _filterType.value = type }

    // --- Projects ---
    val activeProjects: StateFlow<List<ProjectEntity>> = projectRepository.getActiveProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRootProjects: StateFlow<List<ProjectEntity>> = projectRepository.getActiveRootProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selection State ---
    private val _selectedClient = MutableStateFlow<ClientEntity?>(null)
    val selectedClient: StateFlow<ClientEntity?> = _selectedClient

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject

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
                 _transferState.value = TransferState.Success("Exported to ${file.absolutePath}")
            }.onFailure {
                 _transferState.value = TransferState.Error(it.message ?: "Export failed")
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
            val result = transferManager.importData(DataTransferManager.EntityType.CLIENTS, uri, wipe)
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
            val client = ClientEntity(
                firstName = firstName,
                lastName = lastName,
                tipoCliente = tipoCliente,
                razonSocial = razonSocial,
                nifCif = nifCif,
                personaContacto = personaContacto,
                phone = phone,
                email = email,
                calle = calle,
                numero = numero,
                piso = piso,
                poblacion = poblacion,
                codigoPostal = codigoPostal,
                notas = notas,
                categoria = "Potencial" // Default
            )
            repository.createClient(client)
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.createClient(client) // Room Insert(OnConflictStrategy.REPLACE) acts as update
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
                repository.addDocument(doc)
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
            repository.deleteDocument(document)
            attachmentRepository.deleteAttachment(document.fileName)
        }
    }

    fun createProject(clientId: Int, name: String, status: String, startDate: Long, endDate: Long?, category: String? = null) {
        viewModelScope.launch {
            val projectStatus = try {
                com.antigravity.aegis.data.local.entity.ProjectStatus.valueOf(status.uppercase())
            } catch (e: Exception) {
                com.antigravity.aegis.data.local.entity.ProjectStatus.ACTIVE
            }
            val project = ProjectEntity(clientId = clientId, name = name, status = projectStatus, startDate = startDate, endDate = endDate, category = category)
            projectRepository.insertProject(project)
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
            repository.createTask(task)
        }
    }
    
    fun updateTaskStatus(task: TaskEntity, isCompleted: Boolean) {
         viewModelScope.launch {
            repository.updateTaskStatus(task.id, isCompleted)
        }
    }
    // Navigation and Selection
    fun selectClient(clientId: Int) {
        viewModelScope.launch {
            _selectedClient.value = repository.getClientById(clientId)
            
            launch {
                projectRepository.getProjectsByClient(clientId).collect { projects ->
                    _clientProjects.value = projects
                }
            }
            
            launch {
                repository.getDocumentsForClient(clientId).collect { docs ->
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
            combine(_projectBudgets, _projectExpenses) { _, _ -> 
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
    val archivedProjects: StateFlow<List<ProjectEntity>> = projectRepository.getProjectsByStatus(com.antigravity.aegis.data.local.entity.ProjectStatus.ARCHIVED.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reactivateProject(projectId: Int) {
        viewModelScope.launch {
            projectRepository.updateProjectStatus(projectId, com.antigravity.aegis.data.local.entity.ProjectStatus.ACTIVE.name)
        }
    }

    fun selectProject(projectId: Int) {
         viewModelScope.launch {
            _selectedProject.value = projectRepository.getProjectById(projectId)
            // Fetch tasks
            launch {
                repository.getTasksForProject(projectId).collect { tasks ->
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
                status = com.antigravity.aegis.data.local.entity.ProjectStatus.ACTIVE,
                startDate = startDate,
                materials = materials,
                price = price,
                estimatedTime = estimatedTime,
                estimatedTimeUnit = estimatedTimeUnit
            )
            projectRepository.insertProject(subProject)
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

    // --- Template Management ---
    val templates: StateFlow<List<ProjectEntity>> = projectRepository.getTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val defaultCategories = listOf(
        "Reformas", 
        "Marketing Digital", 
        "Sistemas", 
        "Ventas", 
        "Administración", 
        "Otros"
    )

    val templateCategories: StateFlow<List<String>> = projectRepository.getAllCategories()
        .map { dbCategories -> 
            (dbCategories + defaultCategories).distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories.sorted())

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
                .onSuccess { _transferState.value = TransferState.Success("Template Exported") }
                .onFailure { _transferState.value = TransferState.Error(it.message ?: "Export Failed") }
        }
    }

    fun importTemplate(uri: Uri) {
        viewModelScope.launch {
             _transferState.value = TransferState.Loading
            importTemplateUseCase(uri)
                .onSuccess { _transferState.value = TransferState.Success("Template Imported") }
                .onFailure { _transferState.value = TransferState.Error(it.message ?: "Import Failed") }
        }
    }
}
