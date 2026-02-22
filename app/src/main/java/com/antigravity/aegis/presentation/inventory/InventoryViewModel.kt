package com.antigravity.aegis.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.domain.repository.InventoryRepository
import com.antigravity.aegis.domain.util.Result as DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.antigravity.aegis.domain.transfer.DataTransferManager
import android.net.Uri

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val transferManager: DataTransferManager
) : ViewModel() {

    // Transfer Logic
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState = _transferState.asStateFlow()

    fun exportProducts() {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = transferManager.exportData(DataTransferManager.EntityType.INVENTORY)
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
            val errors = transferManager.validateImport(DataTransferManager.EntityType.INVENTORY, uri)
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
            val result = transferManager.importData(DataTransferManager.EntityType.INVENTORY, uri, wipe)
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


    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanned State
    private val _scanState = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanState = _scanState.asStateFlow()

    fun onBarcodeDetected(barcode: String) {
        // Debounce if already processing same barcode? 
        // For simplicity, we just query. UI should pause analysis or show dialog.
        viewModelScope.launch {
            if (_scanState.value is ScanResult.Idle) {
                val product = repository.getProductByBarcode(barcode)
                if (product != null) {
                    _scanState.value = ScanResult.Found(product)
                } else {
                    _scanState.value = ScanResult.NotFound(barcode)
                }
            }
        }
    }

    fun resetScan() {
        _scanState.value = ScanResult.Idle
    }

    fun createProduct(barcode: String, name: String, price: Double, quantity: Int, minQuantity: Int) {
        viewModelScope.launch {
            val newProduct = ProductEntity(
                barcode = barcode,
                name = name,
                description = "",
                quantity = quantity,
                minQuantity = minQuantity,
                price = price
            )
            repository.insertProduct(newProduct)  // Result ignorado intencionalmente — UI no muestra error de creación
            resetScan()
        }
    }

    fun updateQuantity(product: ProductEntity, delta: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + delta).coerceAtLeast(0)
            repository.updateProductQuantity(product.id, newQty)  // Result ignorado — actualización silenciosa
            // Update the local state if we are in "Found" mode so the UI updates immediately
            if (_scanState.value is ScanResult.Found) {
                val currentFound = _scanState.value as ScanResult.Found
                if (currentFound.product.id == product.id) {
                    _scanState.value = ScanResult.Found(product.copy(quantity = newQty))
                }
            }
        }
    }

    sealed class ScanResult {
        object Idle : ScanResult()
        data class Found(val product: ProductEntity) : ScanResult()
        data class NotFound(val barcode: String) : ScanResult()
    }
}
