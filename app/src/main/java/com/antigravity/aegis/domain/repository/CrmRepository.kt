package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.data.model.ExpenseEntity
import com.antigravity.aegis.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

interface CrmRepository {
    // Clients
    suspend fun createClient(client: ClientEntity): Long
    fun getAllClients(): Flow<List<ClientEntity>>
    suspend fun getClientById(id: Int): ClientEntity?

    // Projects
    suspend fun createProject(project: ProjectEntity): Long
    fun getProjectsForClient(clientId: Int): Flow<List<ProjectEntity>>
    suspend fun getProjectById(id: Int): ProjectEntity?
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    // Tasks
    suspend fun createTask(task: TaskEntity): Long
    fun getTasksForProject(projectId: Int): Flow<List<TaskEntity>>
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)

    // Work Reports
    suspend fun createWorkReport(report: WorkReportEntity): Long
    fun getWorkReportsForProject(projectId: Int): Flow<List<WorkReportEntity>>
    fun getAllWorkReports(): Flow<List<WorkReportEntity>>

    // Quotes
    suspend fun createQuote(quote: QuoteEntity): Long
    fun getAllQuotes(): Flow<List<QuoteEntity>>
    fun getQuotesByStatus(status: String): Flow<List<QuoteEntity>>
    suspend fun getQuoteById(id: Int): QuoteEntity?
    suspend fun updateQuoteStatus(quoteId: Int, status: String)

    // Expenses
    suspend fun createExpense(expense: ExpenseEntity): Long
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity>

    // Inventory
    suspend fun createProduct(product: ProductEntity): Long
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    suspend fun updateProductQuantity(id: Int, quantity: Int)
    fun getLowStockProducts(): Flow<List<ProductEntity>>
}
