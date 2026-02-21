package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ExpenseDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.dao.DocumentDao
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.data.local.entity.DocumentEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @deprecated Esta implementación monolítica está siendo reemplazada por implementaciones segregadas:
 * - ClientRepositoryImpl para operaciones de clientes
 * - ProjectRepositoryImpl para operaciones de proyectos
 * - TaskRepositoryImpl para operaciones de tareas
 * - ExpenseRepositoryImpl para operaciones de gastos
 * - BudgetRepositoryImpl para operaciones de presupuestos
 * 
 * Por favor, migra tu código a las implementaciones específicas correspondientes.
 */
@Deprecated(
    message = "Usar ClientRepositoryImpl, ProjectRepositoryImpl, TaskRepositoryImpl, ExpenseRepositoryImpl o BudgetRepositoryImpl según el dominio"
)
@Suppress("DEPRECATION")
class CrmRepositoryImpl @Inject constructor(
    private val dao: CrmDao,
    private val documentDao: DocumentDao,
    private val expenseDao: ExpenseDao
) : CrmRepository {

    override suspend fun createClient(client: ClientEntity): Long {
        return dao.insertClient(client)
    }

    override fun getAllClients(): Flow<List<ClientEntity>> {
        return dao.getAllClients()
    }

    override fun getClientsByType(tipoCliente: String): Flow<List<ClientEntity>> {
        return dao.getClientsByType(tipoCliente)
    }

    override fun searchClients(query: String): Flow<List<ClientEntity>> {
        return dao.searchClients(query)
    }

    override suspend fun getClientById(id: Int): ClientEntity? {
        return dao.getClientById(id)
    }

    override suspend fun updateClientCategoria(clientId: Int, categoria: String) {
        dao.updateClientCategoria(clientId, categoria)
    }

    override suspend fun createProject(project: ProjectEntity): Long {
        return dao.insertProject(project)
    }

    override fun getProjectsForClient(clientId: Int): Flow<List<ProjectEntity>> {
        return dao.getProjectsForClient(clientId)
    }

    override suspend fun getProjectById(id: Int): ProjectEntity? {
        return dao.getProjectById(id)
    }

    override fun getActiveProjects(): Flow<List<ProjectEntity>> {
        return dao.getActiveProjects()
    }

    override suspend fun createTask(task: TaskEntity): Long {
        return dao.insertTask(task)
    }

    override fun getTasksForProject(projectId: Int): Flow<List<TaskEntity>> {
        return dao.getTasksByProject(projectId)
    }

    override suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean) {
        dao.updateTaskStatus(taskId, isCompleted)
    }

    override suspend fun createQuote(quote: QuoteEntity): Long {
        return dao.insertQuote(quote)
    }

    override fun getAllQuotes(): Flow<List<QuoteEntity>> {
        return dao.getAllQuotes()
    }

    override fun getQuotesByStatus(status: String): Flow<List<QuoteEntity>> {
        return dao.getQuotesByStatus(status)
    }

    override suspend fun getQuoteById(id: Int): QuoteEntity? {
        return dao.getQuoteById(id)
    }

    override suspend fun updateQuoteStatus(quoteId: Int, status: String) {
        dao.updateQuoteStatus(quoteId, status)
    }

    override suspend fun createExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    override suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesByDateRangeSync(startDate, endDate)
    }

    override suspend fun getGeneralExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity> {
        return expenseDao.getGeneralExpensesByDateRangeSync(startDate, endDate)
    }

    override fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByProject(projectId)
    }

    override suspend fun getExpensesForProjectSync(projectId: Int): List<ExpenseEntity> {
        return expenseDao.getExpensesByProjectSync(projectId)
    }

    override suspend fun createProduct(product: ProductEntity): Long {
        return dao.insertProduct(product)
    }

    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return dao.getAllProducts()
    }

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return dao.getProductByBarcode(barcode)
    }

    override suspend fun updateProductQuantity(id: Int, quantity: Int) {
        dao.updateProductQuantity(id, quantity)
    }

    override fun getLowStockProducts(): Flow<List<ProductEntity>> {
        return dao.getLowStockProducts()
    }

    override suspend fun addDocument(document: DocumentEntity): Long {
        return documentDao.insertDocument(document)
    }

    override fun getDocumentsForClient(clientId: Int): Flow<List<DocumentEntity>> {
        return documentDao.getDocumentsForClient(clientId)
    }

    override suspend fun getDocumentById(id: Int): DocumentEntity? {
        return documentDao.getDocumentById(id)
    }

    override suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }
}
