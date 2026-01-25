package com.antigravity.aegis.presentation.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.reports.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import com.antigravity.aegis.domain.transfer.DataTransferManager
import com.antigravity.aegis.data.repository.AttachmentRepository
import com.antigravity.aegis.data.model.DocumentEntity
import android.provider.OpenableColumns
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val repository: CrmRepository,
    private val pdfGenerator: PdfGenerator,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val transferManager: DataTransferManager,
    private val attachmentRepository: AttachmentRepository
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
    val activeProjects: StateFlow<List<ProjectEntity>> = repository.getActiveProjects()
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

    // --- Work Reports ---
    private val _projectReports = MutableStateFlow<List<WorkReportEntity>>(emptyList())
    val projectReports: StateFlow<List<WorkReportEntity>> = _projectReports

    val allWorkReports: StateFlow<List<WorkReportEntity>> = repository.getAllWorkReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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

    fun createProject(clientId: Int, name: String, status: String, deadline: Long?) {
        viewModelScope.launch {
            val project = ProjectEntity(clientId = clientId, name = name, status = status, deadline = deadline)
            repository.createProject(project)
        }
    }

    fun createTask(projectId: Int, description: String) {
        viewModelScope.launch {
            val task = TaskEntity(projectId = projectId, description = description)
            repository.createTask(task)
        }
    }
    
    fun updateTaskStatus(task: TaskEntity, isCompleted: Boolean) {
         viewModelScope.launch {
            repository.updateTaskStatus(task.id, isCompleted)
        }
    }
    
    fun createWorkReport(projectId: Int, description: String, signatureBitmap: android.graphics.Bitmap?) {
        viewModelScope.launch {
            // Save signature bitmap to internal storage
            var signaturePath: String? = null
            if (signatureBitmap != null) {
                val filename = "signature_${System.currentTimeMillis()}.png"
                val file = java.io.File(context.filesDir, filename)
                try {
                    java.io.FileOutputStream(file).use { out ->
                        signatureBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                    signaturePath = file.absolutePath
                } catch (e: Exception) {
                    android.util.Log.e("CrmViewModel", "Error saving signature", e)
                }
            }
            
            val report = WorkReportEntity(
                projectId = projectId,
                date = System.currentTimeMillis(),
                description = description,
                signaturePath = signaturePath
            )
            val reportId = repository.createWorkReport(report)
            
            // Generate PDF
            val project = _selectedProject.value
            val client = _selectedClient.value // Might be null if we navigated directly... assume flow is robust
            
            if (project != null && project.id == projectId) {
                // We need client...
                 val currentClient = repository.getClientById(project.clientId)
                 if (currentClient != null) {
                      val pdfFile = pdfGenerator.generateReportPdf(context, report.copy(id = reportId.toInt()), project, currentClient, signatureBitmap)
                      // Notify user or share intent?
                 }
            }
        }
    }

    // Navigation and Selection
    fun selectClient(clientId: Int) {
        viewModelScope.launch {
            _selectedClient.value = repository.getClientById(clientId)
            
            launch {
                repository.getProjectsForClient(clientId).collect { projects ->
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

    fun selectProject(projectId: Int) {
         viewModelScope.launch {
            _selectedProject.value = repository.getProjectById(projectId)
            // Fetch tasks
            launch {
                repository.getTasksForProject(projectId).collect { tasks ->
                    _projectTasks.value = tasks
                }
            }
            // Fetch reports
             launch {
                repository.getWorkReportsForProject(projectId).collect { reports ->
                    _projectReports.value = reports
                }
            }
        }
    }
    
    fun clearSelection() {
        _selectedClient.value = null
        _selectedProject.value = null
    }
}
