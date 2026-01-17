package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.data.model.ExpenseEntity
import com.antigravity.aegis.data.model.ProductEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CrmRepositoryImpl @Inject constructor(
    private val dao: CrmDao
) : CrmRepository {

    override suspend fun createClient(client: ClientEntity): Long {
        return dao.insertClient(client)
    }

    override fun getAllClients(): Flow<List<ClientEntity>> {
        return dao.getAllClients()
    }

    override suspend fun getClientById(id: Int): ClientEntity? {
        return dao.getClientById(id)
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

    override suspend fun createWorkReport(report: WorkReportEntity): Long {
        return dao.insertWorkReport(report)
    }

    override fun getWorkReportsForProject(projectId: Int): Flow<List<WorkReportEntity>> {
        return dao.getWorkReportsForProject(projectId)
    }

    override fun getAllWorkReports(): Flow<List<WorkReportEntity>> {
        return dao.getAllWorkReports()
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
        return dao.insertExpense(expense)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return dao.getAllExpenses()
    }

    override suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity> {
        return dao.getExpensesByDateRange(startDate, endDate)
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
}
