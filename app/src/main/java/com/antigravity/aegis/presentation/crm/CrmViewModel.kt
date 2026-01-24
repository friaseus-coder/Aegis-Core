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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val repository: CrmRepository,
    private val pdfGenerator: PdfGenerator, // Inject PDF Generator
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val transferManager: DataTransferManager
) : ViewModel() {

    // --- Clients ---
    val allClients: StateFlow<List<ClientEntity>> = repository.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    fun createClient(name: String, email: String?, phone: String?, notes: String?) {
        viewModelScope.launch {
            val client = ClientEntity(name = name, email = email, phone = phone, notes = notes)
            repository.createClient(client)
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
            repository.getProjectsForClient(clientId).collect { projects ->
                _clientProjects.value = projects
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
