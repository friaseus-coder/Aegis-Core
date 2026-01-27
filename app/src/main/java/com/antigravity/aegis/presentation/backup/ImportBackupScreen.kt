package com.antigravity.aegis.presentation.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ImportBackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Launcher for Creating Backup (Export)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
             try {
                val outputStream = context.contentResolver.openOutputStream(it)
                viewModel.createBackup(outputStream)
             } catch (e: Exception) {
                 // Handle error
             }
        }
    }

    // Launcher for Restoring Backup (Import)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
             try {
                val inputStream = context.contentResolver.openInputStream(it)
                viewModel.restoreBackup(inputStream)
             } catch (e: Exception) {
                 // Handle error
             }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Importar / Exportar Datos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { importLauncher.launch("application/json") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Importar Copia de Seguridad")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { exportLauncher.launch("aegis_backup_${System.currentTimeMillis()}.json") },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar Copia de Seguridad")
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
        
        status?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(it, color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Volver")
        }
    }
}
