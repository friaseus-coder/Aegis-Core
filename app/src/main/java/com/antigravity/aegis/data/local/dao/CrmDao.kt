package com.antigravity.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.WorkReportEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.data.local.entity.MileageLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * @deprecated Este DAO monolítico está siendo reemplazado por DAOs segregados:
 * - ClientDao para operaciones de clientes
 * - ProjectDao para operaciones de proyectos
 * - TaskDao para operaciones de tareas
 * - ExpenseDao para operaciones de gastos
 * - BudgetDao para operaciones de presupuestos
 * 
 * Por favor, migra tu código a los DAOs específicos correspondientes.
 */
@Deprecated(
    message = "Usar ClientDao, ProjectDao, TaskDao, ExpenseDao o BudgetDao según el dominio",
    replaceWith = ReplaceWith("ClientDao, ProjectDao, TaskDao")
)
@Dao
interface CrmDao {
    // Clients
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Query("SELECT * FROM clients ORDER BY firstName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE tipoCliente = :tipoCliente ORDER BY firstName ASC")
    fun getClientsByType(tipoCliente: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' ORDER BY firstName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Int): ClientEntity?

    @Query("UPDATE clients SET categoria = :categoria WHERE id = :clientId")
    suspend fun updateClientCategoria(clientId: Int, categoria: String)

    // Projects
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Query("SELECT * FROM projects WHERE clientId = :clientId")
    fun getProjectsForClient(clientId: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Query("SELECT * FROM projects WHERE status = 'Active'")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>


    // Tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY isCompleted ASC")
    fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>



    
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)

    // Work Reports
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkReport(report: WorkReportEntity): Long

    @Query("SELECT * FROM work_reports WHERE projectId = :projectId ORDER BY date DESC")
    fun getWorkReportsForProject(projectId: Int): Flow<List<WorkReportEntity>>

    @Query("SELECT * FROM work_reports ORDER BY date DESC")
    fun getAllWorkReports(): Flow<List<WorkReportEntity>>

    // Quotes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Query("SELECT * FROM quotes ORDER BY date DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE status = :status ORDER BY date DESC")
    fun getQuotesByStatus(status: String): Flow<List<QuoteEntity>>
    
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): QuoteEntity?

    @Query("UPDATE quotes SET status = :status WHERE id = :quoteId")
    suspend fun updateQuoteStatus(quoteId: Int, status: String)

    // Expenses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity>

    // Inventory
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("UPDATE products SET quantity = :quantity WHERE id = :id")
    suspend fun updateProductQuantity(id: Int, quantity: Int)

    @Query("SELECT * FROM products WHERE quantity < minQuantity")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    // Mileage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageLog(log: MileageLogEntity): Long

    @Query("SELECT * FROM mileage_logs ORDER BY date DESC")
    fun getAllMileageLogs(): Flow<List<MileageLogEntity>>

    // Wipe Methods for Import (Caution!)
    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM mileage_logs")
    suspend fun deleteAllMileageLogs()

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
    // Sync methods for Backup
    @Query("SELECT * FROM clients")
    suspend fun getAllClientsSync(): List<ClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)
    
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Query("SELECT * FROM work_reports")
    suspend fun getAllWorkReportsSync(): List<WorkReportEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkReports(reports: List<WorkReportEntity>)
    
    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotesSync(): List<QuoteEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)
    
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    @Query("SELECT * FROM products")
    suspend fun getAllProductsSync(): List<ProductEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Query("SELECT * FROM mileage_logs")
    suspend fun getAllMileageLogsSync(): List<MileageLogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageLogs(logs: List<MileageLogEntity>)
    
    // Wipe remaining tables
    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    @Query("DELETE FROM work_reports")
    suspend fun deleteAllWorkReports()
}
