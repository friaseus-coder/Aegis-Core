package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check

@Composable
fun SetupScreen(
    state: SetupUiState,
    onConfirm: (String, String, String, com.antigravity.aegis.data.model.UserRole) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("es") }
    
    var isEmpresa by remember { mutableStateOf(false) }
    var isParticular by remember { mutableStateOf(true) } // Default to Worker
    
    // In a real app, this would be a multi-step wizard:
    // ...
    // For this demo, we show everything in one scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding() // Avoid keyboard overlap
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.recovery_kit_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.recovery_kit_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.seedPhrase.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Email Share Button
                val context = androidx.compose.ui.platform.LocalContext.current
                OutlinedButton(
                    onClick = {
                        val seedText = state.seedPhrase.mapIndexed { index, word -> "${index + 1}. $word" }.joinToString("\n")
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Aegis Core - Kit de Recuperación")
                            putExtra(android.content.Intent.EXTRA_TEXT, "Guarda esta frase semilla en un lugar seguro:\n\n$seedText")
                        }
                        try {
                            context.startActivity(android.content.Intent.createChooser(intent, "Enviar Kit de Recuperación"))
                        } catch (e: Exception) {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TITLE, "Aegis Core - Kit de Recuperación")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Frase Semilla:\n\n$seedText")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir Kit"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Kit por Correo")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            
                val rows = state.seedPhrase.chunked(3)
                rows.forEachIndexed { rowIndex, rowWords ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowWords.forEachIndexed { colIndex, word ->
                            val realIndex = (rowIndex * 3) + colIndex + 1
                            Box(modifier = Modifier.weight(1f)) {
                                SeedWordChip(realIndex, word)
                            }
                        }
                        // Fill empty space if last row incomplete
                        if (rowWords.size < 3) {
                             repeat(3 - rowWords.size) { Box(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Información del Administrador")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Idioma Preferido", style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = language == "es",
                onClick = { language = "es" },
                label = { Text("Español") },
                leadingIcon = if (language == "es") {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
            FilterChip(
                selected = language == "en",
                onClick = { language = "en" },
                label = { Text("English") },
                leadingIcon = if (language == "en") {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            label = { Text(stringResource(R.string.pin_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Perfiles Activos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isEmpresa,
                        onCheckedChange = { isEmpresa = it }
                    )
                    Column {
                        Text("Empresa / Autónomo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Acceso total y gestión", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isParticular,
                        onCheckedChange = { isParticular = it }
                    )
                     Column {
                        Text("Particular / Trabajador", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Acceso operativo limitado", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val isValid = name.isNotBlank() && pin.length >= 4 && (isEmpresa || isParticular)
        Button(
            onClick = { 
                 val role = when {
                    isEmpresa && isParticular -> com.antigravity.aegis.data.model.UserRole.ADMIN
                    isEmpresa -> com.antigravity.aegis.data.model.UserRole.MANAGER
                    else -> com.antigravity.aegis.data.model.UserRole.USER
                }
                onConfirm(name, language, pin, role) 
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.init_vault_button))
        }
    }
}

@Composable
fun SeedWordChip(index: Int, word: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = word,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
