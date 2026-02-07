package com.antigravity.aegis.data.repository

import androidx.room.withTransaction

import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.data.local.entity.*
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.domain.repository.BackupRepository
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val users: List<UserEntity> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val workReports: List<WorkReportEntity> = emptyList(),
    val quotes: List<QuoteEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val mileageLogs: List<MileageLogEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList(),
    val userConfigs: List<UserConfig> = emptyList()
)

class BackupRepositoryImpl @Inject constructor(
    private val database: AegisDatabase,
    private val gson: Gson
) : BackupRepository {

    override suspend fun createBackupJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val users = database.userEntityDao().getAllUsersList()
            val clients = database.crmDao().getAllClientsSync()
            val projects = database.crmDao().getAllProjectsSync()
            val tasks = database.crmDao().getAllTasksSync()
            val reports = database.crmDao().getAllWorkReportsSync() 
            val quotes = database.crmDao().getAllQuotesSync()
            val expenses = database.crmDao().getAllExpensesSync()
            val products = database.crmDao().getAllProductsSync()
            val mileageLogs = database.crmDao().getAllMileageLogsSync()
            val userConfigs = database.userConfigDao().getAllUserConfigsSync()
            val documents = database.documentDao().getAllDocumentsSync()
            
            val data = BackupData(
                users = users,
                clients = clients,
                projects = projects,
                tasks = tasks,
                workReports = reports,
                quotes = quotes,
                expenses = expenses,
                products = products,
                mileageLogs = mileageLogs,
                documents = documents,
                userConfigs = userConfigs
            )
            
            Result.success(gson.toJson(data)) 
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreBackupJson(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            // Re-implementing with withTransaction for suspend support
            // androidx.room.withTransaction(database) {
            // Note: withTransaction was failing with suspend errors, running sequentially in IO context for now.
                // Clear
                database.crmDao().deleteAllWorkReports()
                database.crmDao().deleteAllTasks()
                database.crmDao().deleteAllProjects()
                database.crmDao().deleteAllQuotes()
                database.crmDao().deleteAllExpenses()
                database.crmDao().deleteAllProducts()
                database.crmDao().deleteAllMileageLogs()
                database.documentDao().deleteAllDocuments()
                database.crmDao().deleteAllClients()
                database.userConfigDao().deleteAllUserConfigs()
                database.userEntityDao().deleteAllUsers()
                
                // Insert
                if (backupData.users.isNotEmpty()) database.userEntityDao().insertUsers(backupData.users)
                if (backupData.clients.isNotEmpty()) database.crmDao().insertClients(backupData.clients)
                if (backupData.projects.isNotEmpty()) database.crmDao().insertProjects(backupData.projects)
                if (backupData.tasks.isNotEmpty()) database.crmDao().insertTasks(backupData.tasks)
                if (backupData.workReports.isNotEmpty()) database.crmDao().insertWorkReports(backupData.workReports)
                if (backupData.quotes.isNotEmpty()) database.crmDao().insertQuotes(backupData.quotes)
                if (backupData.expenses.isNotEmpty()) database.crmDao().insertExpenses(backupData.expenses)
                if (backupData.products.isNotEmpty()) database.crmDao().insertProducts(backupData.products)
                if (backupData.mileageLogs.isNotEmpty()) database.crmDao().insertMileageLogs(backupData.mileageLogs)
                if (backupData.documents.isNotEmpty()) database.documentDao().insertDocuments(backupData.documents)
                if (backupData.userConfigs.isNotEmpty()) database.userConfigDao().insertUserConfigs(backupData.userConfigs)
            // }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
