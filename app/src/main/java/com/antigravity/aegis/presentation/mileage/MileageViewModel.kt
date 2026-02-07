package com.antigravity.aegis.presentation.mileage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.dao.UserConfigDao
import com.antigravity.aegis.data.local.entity.MileageLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

import com.antigravity.aegis.domain.transfer.DataTransferManager
import android.net.Uri

@HiltViewModel
class MileageViewModel @Inject constructor(
    private val crmDao: CrmDao,
    private val userConfigDao: UserConfigDao,
    @ApplicationContext private val context: Context,
    private val transferManager: DataTransferManager
) : ViewModel() {

    // Transfer Logic
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState = _transferState.asStateFlow()

    fun exportMileage() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.exportData(DataTransferManager.EntityType.MILEAGE)
            result.onSuccess { file ->
                 _transferState.value = TransferState.Success("Exported to ${file.absolutePath}")
            }.onFailure {
                 _transferState.value = TransferState.Error(it.message ?: "Export failed")
            }
        }
    }

    fun validateImport(uri: Uri) {
         viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val errors = transferManager.validateImport(DataTransferManager.EntityType.MILEAGE, uri)
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
            val result = transferManager.importData(DataTransferManager.EntityType.MILEAGE, uri, wipe)
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
        data class ValidationSuccess(val uri: Uri) : TransferState()
    }


    val userConfig = userConfigDao.getUserConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val logs = crmDao.getAllMileageLogs()
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus

    fun updatePricePerKm(price: Double) {
        viewModelScope.launch {
            userConfigDao.updatePricePerKm(price)
        }
    }

    fun saveTrip(
        date: Long,
        origin: String,
        destination: String,
        vehicle: String,
        startOdo: Double,
        endOdo: Double,
        currentPrice: Double
    ) {
        viewModelScope.launch {
            val distance = endOdo - startOdo
            if (distance <= 0) return@launch // Basic validation

            val cost = distance * currentPrice
            
            val log = MileageLogEntity(
                date = date,
                origin = origin,
                destination = destination,
                vehicle = vehicle,
                startOdometer = startOdo,
                endOdometer = endOdo,
                distanceKm = distance,
                pricePerKmSnapshot = currentPrice,
                calculatedCost = cost
            )
            crmDao.insertMileageLog(log)
        }
    }

    fun exportAnnualReport() {
        viewModelScope.launch {
            _exportStatus.value = "Exporting..."
            try {
                val allLogs = logs.value
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                
                val csvFile = File(exportDir, "Mileage_Annual_Report_${System.currentTimeMillis()}.csv")
                
                FileWriter(csvFile).use { writer ->
                    writer.append("Date,Origin,Destination,Vehicle,Start Odo,End Odo,Distance (km),Price/Km,Cost\n")
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    
                    for (log in allLogs) {
                        writer.append(
                            "${sdf.format(java.util.Date(log.date))}," +
                            "\"${log.origin}\"," +
                            "\"${log.destination}\"," +
                            "\"${log.vehicle}\"," +
                            "${log.startOdometer}," +
                            "${log.endOdometer}," +
                            "${log.distanceKm}," +
                            "${log.pricePerKmSnapshot}," +
                            "${log.calculatedCost}\n"
                        )
                    }
                }
                _exportStatus.value = "Exported to: ${csvFile.absolutePath}"
            } catch (e: Exception) {
                _exportStatus.value = "Error: ${e.message}"
            }
        }
    }
    
    fun clearExportStatus() {
        _exportStatus.value = null
    }
}
