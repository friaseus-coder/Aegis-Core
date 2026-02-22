package com.antigravity.aegis.presentation.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.usecase.CreateBackupUseCase
import com.antigravity.aegis.domain.usecase.RestoreBackupUseCase
import com.antigravity.aegis.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.InputStream
import java.io.OutputStream

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase
) : ViewModel() {

    private val _status = MutableStateFlow<String?>(null)
    val status = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun createBackup(outputStream: OutputStream?) {
        if (outputStream == null) {
            _status.value = "Error: Invalid output stream"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = createBackupUseCase()) {
                is Result.Success -> {
                    try {
                        outputStream.use { it.write(result.data.toByteArray()) }
                        _status.value = "Copia de seguridad creada correctamente"
                    } catch (e: Exception) {
                        _status.value = "Error guardando archivo: ${e.message}"
                    }
                }
                is Result.Error -> {
                    _status.value = "Error creando backup: ${result.exception.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun restoreBackup(inputStream: InputStream?) {
        if (inputStream == null) {
            _status.value = "Error: Invalid input stream"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val json = inputStream.bufferedReader().use { it.readText() }
                when (val result = restoreBackupUseCase(json)) {
                    is Result.Success -> _status.value = "Base de datos restaurada correctamente"
                    is Result.Error -> _status.value = "Error restaurando: ${result.exception.message}"
                }
            } catch (e: Exception) {
                _status.value = "Error leyendo archivo: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}

