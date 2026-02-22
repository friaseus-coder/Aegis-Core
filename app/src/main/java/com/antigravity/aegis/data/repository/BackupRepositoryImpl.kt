package com.antigravity.aegis.data.repository

import androidx.room.withTransaction

import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.data.local.entity.*
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.domain.repository.BackupRepository
import com.antigravity.aegis.domain.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.antigravity.aegis.data.util.ZipManager
import com.antigravity.aegis.data.security.FileCryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val clients: List<ClientEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val quotes: List<QuoteEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val mileageLogs: List<MileageLogEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList(),
    val userConfigs: List<UserConfig> = emptyList()
)

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AegisDatabase,
    private val gson: Gson,
    private val zipManager: ZipManager,
    private val fileCryptoManager: FileCryptoManager
) : BackupRepository {

    override suspend fun createBackupJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val clients = database.crmDao().getAllClientsSync()
            val projects = database.crmDao().getAllProjectsSync()
            val tasks = database.crmDao().getAllTasksSync()
            val quotes = database.crmDao().getAllQuotesSync()
            val expenses = database.crmDao().getAllExpensesSync()
            val products = database.crmDao().getAllProductsSync()
            val mileageLogs = database.crmDao().getAllMileageLogsSync()
            val userConfigs = database.userConfigDao().getAllUserConfigsSync()
            val documents = database.documentDao().getAllDocumentsSync()

            val data = BackupData(
                clients = clients,
                projects = projects,
                tasks = tasks,
                quotes = quotes,
                expenses = expenses,
                products = products,
                mileageLogs = mileageLogs,
                documents = documents,
                userConfigs = userConfigs
            )

            Result.Success(gson.toJson(data))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun restoreBackupJson(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Clear
            database.crmDao().deleteAllTasks()
            database.crmDao().deleteAllProjects()
            database.crmDao().deleteAllQuotes()
            database.crmDao().deleteAllExpenses()
            database.crmDao().deleteAllProducts()
            database.crmDao().deleteAllMileageLogs()
            database.documentDao().deleteAllDocuments()
            database.crmDao().deleteAllClients()
            database.userConfigDao().deleteAllUserConfigs()

            // Insert
            if (backupData.clients.isNotEmpty()) database.crmDao().insertClients(backupData.clients)
            if (backupData.projects.isNotEmpty()) database.crmDao().insertProjects(backupData.projects)
            if (backupData.tasks.isNotEmpty()) database.crmDao().insertTasks(backupData.tasks)
            if (backupData.quotes.isNotEmpty()) database.crmDao().insertQuotes(backupData.quotes)
            if (backupData.expenses.isNotEmpty()) database.crmDao().insertExpenses(backupData.expenses)
            if (backupData.products.isNotEmpty()) database.crmDao().insertProducts(backupData.products)
            if (backupData.mileageLogs.isNotEmpty()) database.crmDao().insertMileageLogs(backupData.mileageLogs)
            if (backupData.documents.isNotEmpty()) database.documentDao().insertDocuments(backupData.documents)
            if (backupData.userConfigs.isNotEmpty()) database.userConfigDao().insertUserConfigs(backupData.userConfigs)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createFullBackupZip(): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 1. Create JSON Backup
            val jsonResult = createBackupJson()
            if (jsonResult is Result.Error) return@withContext Result.Error(jsonResult.exception)
            val json = (jsonResult as Result.Success).data
            
            val tempDir = File(context.cacheDir, "backup_temp")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            // 2. Save JSON to file
            val jsonFile = File(tempDir, "data.json")
            jsonFile.writeText(json)

            // 3. Collect attachments and PDFs
            // Based on earlier research: context.filesDir contains encrypted attachments and subfolders like 'quotes'
            val filesToZip = mutableListOf<File>()
            filesToZip.add(jsonFile)
            
            val appFiles = context.filesDir.listFiles()
            appFiles?.forEach { file ->
                // Skip internal system files if any, but include our data
                if (file.name != "datastore" && file.name != "no_backup") {
                    filesToZip.add(file)
                }
            }

            // 4. Create ZIP
            val unencryptedZip = File(context.cacheDir, "backup_unencrypted.zip")
            zipManager.zipFiles(filesToZip, unencryptedZip)

            // 5. Encrypt ZIP
            val finalZip = File(context.filesDir, "aegis_backup_${System.currentTimeMillis()}.zip.enc")
            val outputStream = FileOutputStream(finalZip)
            
            // We use a fixed internal password for automated backups for now, 
            // or we could derive it from MasterKey. 
            // For simplicity in this phase, let's use a "cloud_sync" tag that we can reconstruct.
            fileCryptoManager.encrypt(outputStream, unencryptedZip.readBytes(), "aegis_cloud_sync_v1")

            // Cleanup
            tempDir.deleteRecursively()
            unencryptedZip.delete()

            Result.Success(finalZip)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

