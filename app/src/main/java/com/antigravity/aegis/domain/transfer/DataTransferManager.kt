package com.antigravity.aegis.domain.transfer

import android.net.Uri
import android.content.Context
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.model.*
import com.antigravity.aegis.domain.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class DataTransferManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crmDao: CrmDao,
    private val backupRepository: BackupRepository
) {

    enum class EntityType {
        CLIENTS, PROJECTS, INVENTORY, EXPENSES, MILEAGE, QUOTES
    }

    suspend fun validateImport(type: EntityType, uri: Uri): List<String> = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val rows = CsvHelper.parse(inputStream)
                if (rows.isEmpty()) return@withContext listOf("File is empty")

                // Validation Logic
                val header = rows[0].keys.toList() // Approximation, actually parse returns maps with same keys
                // We should validate header structure
                
                when (type) {
                    EntityType.CLIENTS -> {
                        errors.addAll(CsvHelper.validateHeader(header, listOf("firstName"))) // Minimal check
                        rows.forEachIndexed { idx, row ->
                            if (row["firstName"].isNullOrBlank() && row["name"].isNullOrBlank()) errors.add("Row ${idx + 1}: Missing name")
                        }
                    }
                    EntityType.INVENTORY -> {
                        errors.addAll(CsvHelper.validateHeader(header, listOf("barcode", "name", "quantity", "price")))
                        rows.forEachIndexed { idx, row ->
                            if (row["barcode"].isNullOrBlank()) errors.add("Row ${idx + 1}: Missing barcode")
                            if (row["price"]?.toDoubleOrNull() == null) errors.add("Row ${idx + 1}: Invalid price")
                        }
                    }
                    // Add other validaters...
                    else -> {}
                }
            } ?: return@withContext listOf("Could not open file")
        } catch (e: Exception) {
            errors.add("Parse error: ${e.message}")
        }
        errors
    }

    suspend fun importData(type: EntityType, uri: Uri, wipeExisting: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
                if (wipeExisting) {
                    val backupResult = backupRepository.createAutoBackup("pre_import_${type.name.lowercase()}")
                    if (backupResult.isFailure) throw Exception("Safety backup failed: ${backupResult.exceptionOrNull()?.message}")
                    
                    // Wipe Table
                     when (type) {
                        EntityType.CLIENTS -> crmDao.deleteAllClients()
                        EntityType.INVENTORY -> crmDao.deleteAllProducts()
                        EntityType.EXPENSES -> crmDao.deleteAllExpenses()
                        EntityType.MILEAGE -> crmDao.deleteAllMileageLogs()
                        EntityType.QUOTES -> crmDao.deleteAllQuotes()
                        else -> {}
                    }
                }

                // 2. Read Data
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val rows = CsvHelper.parse(inputStream)
                
                    // 3. Insert
                     when (type) {
                        EntityType.CLIENTS -> {
                            rows.forEach { row ->
                                val entity = ClientEntity(
                                    firstName = row["firstName"] ?: row["name"] ?: "",
                                    lastName = row["lastName"] ?: "",
                                    tipoCliente = row["type"] ?: "Particular",
                                    razonSocial = row["companyName"],
                                    nifCif = row["nif"],
                                    email = row["email"],
                                    phone = row["phone"],
                                    notas = row["notes"],
                                    calle = row["address"] // Simple mapping to calle for now
                                )
                                crmDao.insertClient(entity)
                            }
                        }
                        EntityType.INVENTORY -> {
                             rows.forEach { row ->
                                val entity = ProductEntity(
                                    barcode = row["barcode"] ?: "",
                                    name = row["name"] ?: "",
                                    price = row["price"]?.toDoubleOrNull() ?: 0.0,
                                    quantity = row["quantity"]?.toIntOrNull() ?: 0,
                                    minQuantity = row["minQuantity"]?.toIntOrNull() ?: 5,
                                    description = row["description"]
                                )
                                crmDao.insertProduct(entity)
                            }
                        }
                        EntityType.EXPENSES -> {
                             rows.forEach { row ->
                                val entity = ExpenseEntity(
                                    date = row["date"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                    totalAmount = row["totalAmount"]?.toDoubleOrNull() ?: 0.0,
                                    merchantName = row["merchantName"],
                                    status = row["status"] ?: "Pending",
                                    imagePath = null // Cannot import images from CSV easily
                                )
                                crmDao.insertExpense(entity)
                            }
                        }
                        EntityType.MILEAGE -> {
                             rows.forEach { row ->
                                val entity = MileageLogEntity(
                                    date = row["date"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                    origin = row["origin"] ?: "",
                                    destination = row["destination"] ?: "",
                                    vehicle = row["vehicle"] ?: "Imported",
                                    distanceKm = row["distance"]?.toDoubleOrNull() ?: 0.0,
                                    calculatedCost = row["cost"]?.toDoubleOrNull() ?: 0.0,
                                    startOdometer = 0.0, endOdometer = 0.0, pricePerKmSnapshot = 0.0
                                )
                                crmDao.insertMileageLog(entity)
                            }
                        }
                        EntityType.QUOTES -> {
                             rows.forEach { row ->
                                val entity = QuoteEntity(
                                    date = row["date"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                    title = row["title"] ?: "",
                                    totalAmount = row["totalAmount"]?.toDoubleOrNull() ?: 0.0,
                                    status = row["status"] ?: "Draft",
                                    description = row["description"] ?: "",
                                    clientId = 0 // TBD: handle linking imports?
                                )
                                crmDao.insertQuote(entity)
                            }
                        }
                         // Add others...
                        else -> {}
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
    
    suspend fun exportData(type: EntityType): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, "${type.name.lowercase()}_export_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                 when (type) {
                    EntityType.CLIENTS -> {
                        writer.append("firstName,lastName,type,companyName,nif,email,phone,address,notes\n")
                        val data = crmDao.getAllClients().first()
                        data.forEach { 
                            val address = listOfNotNull(it.calle, it.numero, it.poblacion).joinToString(" ")
                            writer.append("\"${it.firstName}\",\"${it.lastName}\",\"${it.tipoCliente}\",\"${it.razonSocial ?: ""}\",\"${it.nifCif ?: ""}\",\"${it.email ?: ""}\",\"${it.phone ?: ""}\",\"$address\",\"${it.notas ?: ""}\"\n") 
                        }
                    }
                    EntityType.INVENTORY -> {
                        writer.append("barcode,name,price,quantity,minQuantity,description\n")
                        val data = crmDao.getAllProducts().first()
                        data.forEach {
                             writer.append("\"${it.barcode}\",\"${it.name}\",${it.price},${it.quantity},${it.minQuantity},\"${it.description}\"\n")
                        }
                    }
                    EntityType.EXPENSES -> {
                        writer.append("date,totalAmount,merchantName,status\n")
                        val data = crmDao.getAllExpenses().first()
                        data.forEach {
                            writer.append("${it.date},${it.totalAmount},\"${it.merchantName}\",\"${it.status}\"\n")
                        }
                    }
                    EntityType.MILEAGE -> {
                        writer.append("date,origin,destination,vehicle,distance,cost\n")
                        val data = crmDao.getAllMileageLogs().first()
                        data.forEach {
                            writer.append("${it.date},\"${it.origin}\",\"${it.destination}\",\"${it.vehicle}\",${it.distanceKm},${it.calculatedCost}\n")
                        }
                    }
                    EntityType.QUOTES -> {
                        writer.append("date,title,totalAmount,status,description\n")
                        val data = crmDao.getAllQuotes().first()
                        data.forEach {
                            writer.append("${it.date},\"${it.title}\",${it.totalAmount},\"${it.status}\",\"${it.description}\"\n")
                        }
                    }
                     // Add others...
                    else -> {}
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
