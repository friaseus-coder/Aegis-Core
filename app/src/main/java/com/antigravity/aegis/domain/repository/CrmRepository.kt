package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * @deprecated Este repositorio monolítico está siendo reemplazado por repositorios segregados:
 * - ClientRepository para operaciones de clientes
 * - ProjectRepository para operaciones de proyectos
 * - TaskRepository para operaciones de tareas
 * - ExpenseRepository para operaciones de gastos
 * - BudgetRepository para operaciones de presupuestos
 * 
 * Por favor, migra tu código a los repositorios específicos correspondientes.
 */
@Deprecated(
    message = "Usar ClientRepository, ProjectRepository, TaskRepository, ExpenseRepository o BudgetRepository según el dominio"
)
interface CrmRepository {
    // Clients
    suspend fun createClient(client: ClientEntity): Long
    fun getAllClients(): Flow<List<ClientEntity>>
    fun getClientsByType(tipoCliente: String): Flow<List<ClientEntity>>
    fun searchClients(query: String): Flow<List<ClientEntity>>
    suspend fun getClientById(id: Int): ClientEntity?
    suspend fun updateClientCategoria(clientId: Int, categoria: String)

    // Projects
    suspend fun createProject(project: ProjectEntity): Long
    fun getProjectsForClient(clientId: Int): Flow<List<ProjectEntity>>
    suspend fun getProjectById(id: Int): ProjectEntity?
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    // Tasks
    suspend fun createTask(task: TaskEntity): Long
    fun getTasksForProject(projectId: Int): Flow<List<TaskEntity>>
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)

    // Quotes
    suspend fun createQuote(quote: QuoteEntity): Long
    fun getAllQuotes(): Flow<List<QuoteEntity>>
    fun getQuotesByStatus(status: String): Flow<List<QuoteEntity>>
    suspend fun getQuoteById(id: Int): QuoteEntity?
    suspend fun updateQuoteStatus(quoteId: Int, status: String)

    // Expenses
    suspend fun createExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(expense: ExpenseEntity)
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>>
    suspend fun getExpensesForProjectSync(projectId: Int): List<ExpenseEntity>
    suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity>
    suspend fun getGeneralExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity>

    // Inventory
    suspend fun createProduct(product: ProductEntity): Long
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    suspend fun updateProductQuantity(id: Int, quantity: Int)
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    // Documents
    suspend fun addDocument(document: DocumentEntity): Long
    fun getDocumentsForClient(clientId: Int): Flow<List<DocumentEntity>>
    suspend fun getDocumentById(id: Int): DocumentEntity?
    suspend fun deleteDocument(document: DocumentEntity)
}
