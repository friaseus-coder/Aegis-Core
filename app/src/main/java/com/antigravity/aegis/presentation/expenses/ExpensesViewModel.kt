package com.antigravity.aegis.presentation.expenses

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.domain.expenses.ExportManager
import com.antigravity.aegis.domain.expenses.OcrManager
import com.antigravity.aegis.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

import com.antigravity.aegis.domain.transfer.DataTransferManager

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val projectRepository: com.antigravity.aegis.domain.repository.ProjectRepository,
    private val budgetRepository: com.antigravity.aegis.domain.repository.BudgetRepository,
    private val ocrManager: OcrManager,
    private val exportManager: ExportManager,
    @ApplicationContext private val context: Context,
    private val transferManager: DataTransferManager
) : ViewModel() {

    // ... Transfer Logic (unchanged) ...
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState = _transferState.asStateFlow()

    fun exportExpenses() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.exportData(DataTransferManager.EntityType.EXPENSES)
            result.onSuccess { file ->
                 _transferState.value = TransferState.Success(resId = com.antigravity.aegis.R.string.data_export_success_path, arg = file.absolutePath)
            }.onFailure {
                 _transferState.value = TransferState.Error(message = it.message, resId = com.antigravity.aegis.R.string.data_export_failed)
            }
        }
    }

    fun validateImport(uri: Uri) {
         viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val errors = transferManager.validateImport(DataTransferManager.EntityType.EXPENSES, uri)
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
            val result = transferManager.importData(DataTransferManager.EntityType.EXPENSES, uri, wipe)
            result.onSuccess {
                 _transferState.value = TransferState.Success(resId = com.antigravity.aegis.R.string.data_import_success)
            }.onFailure {
                 _transferState.value = TransferState.Error(message = it.message, resId = com.antigravity.aegis.R.string.data_import_db_error)
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
        data class ValidationSuccess(val uri: Uri) : TransferState()
    }

    // Projects for Dropdown
    val activeProjects: StateFlow<List<com.antigravity.aegis.data.local.entity.ProjectEntity>> = projectRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scannedData = MutableStateFlow<OcrManager.ExtractedData?>(null)
    val scannedData: StateFlow<OcrManager.ExtractedData?> = _scannedData

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus

    fun processImage(uri: Uri) {
        viewModelScope.launch {
             val data = ocrManager.analyzeTicket(context, uri)
             _scannedData.value = data
        }
    }
    
    fun clearScannedData() {
        _scannedData.value = null
        ocrManager.cleanupCacheFiles(context)
    }

    fun saveExpense(date: Long, amount: Double, merchant: String, imageUri: Uri?, category: String, projectId: Int?) {
        viewModelScope.launch {
            var imagePath: String? = imageUri?.toString()
            
            // Persist image if it's in cache
            if (imageUri != null && imageUri.toString().contains("cache")) {
                try {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val fileName = "expense_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, fileName)
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    imagePath = Uri.fromFile(file).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            val expense = ExpenseEntity(
                date = date,
                totalAmount = amount,
                merchantName = merchant,
                imagePath = imagePath,
                category = category,
                projectId = projectId
            )
            expenseRepository.insertExpense(expense)
            clearScannedData()
        }
    }

    /**
     * Distributes an expense across selected projects proportionally to their total budget.
     * If a project has no budget, it gets a base share.
     */
    fun distributeExpense(expense: ExpenseEntity, projectIds: List<Int>) {
        if (projectIds.isEmpty()) return
        
        viewModelScope.launch {
            // 1. Get Budgets for all targets
            val projectBudgets = projectIds.associateWith { pid ->
                budgetRepository.getQuotesByProjectSuspend(pid).filter { it.status == "Accepted" }.sumOf { it.totalAmount }
            }
            
            val totalBudgetPool = projectBudgets.values.sum()
            
            // 2. Distribute
            projectIds.forEach { pid ->
                val projectBudget = projectBudgets[pid] ?: 0.0
                val ratio = if (totalBudgetPool > 0) projectBudget / totalBudgetPool else 1.0 / projectIds.size
                val shareAmount = expense.totalAmount * ratio
                
                // 3. Create Child Expense
                val childExpense = expense.copy(
                    id = 0, // New ID
                    totalAmount = shareAmount,
                    projectId = pid,
                    category = "${expense.category} (Reparto)",
                    merchantName = "${expense.merchantName ?: "Gasto"} [${String.format("%.1f", ratio * 100)}%]"
                )
                expenseRepository.insertExpense(childExpense)
            }
            
            // 4. Update Original as "Distributed"
            val updatedOriginal = expense.copy(status = "Distributed")
            expenseRepository.updateExpense(updatedOriginal)
        }
    }

    fun exportQuarter() {
        viewModelScope.launch {
            _exportStatus.value = "Exporting..."
            try {
                // Calculate current quarter start/end
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                val quarterStartMonth = (currentMonth / 3) * 3
                
                calendar.set(currentYear, quarterStartMonth, 1, 0, 0, 0)
                val startDate = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 3)
                calendar.add(Calendar.MILLISECOND, -1)
                val endDate = calendar.timeInMillis
                
                val expenses = expenseRepository.getExpensesByDateRangeSync(startDate, endDate)
                
                if (expenses.isEmpty()) {
                    _exportStatus.value = context.getString(R.string.data_no_expenses_quarter)
                    return@launch
                }

                val zipFile = exportManager.exportToZip(context, expenses, startDate, endDate)
                _exportStatus.value = context.getString(R.string.data_export_ready, zipFile.absolutePath)
                // Real app would trigger Sharing Intent here
            } catch (e: Exception) {
               _exportStatus.value = context.getString(R.string.general_error_prefix, e.message ?: "")
            }
        }
    }
    
    fun clearExportStatus() {
        _exportStatus.value = null
    }
}
