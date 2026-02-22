package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun SetupScreen(
    state: SetupUiState,
    onConfirm: (String) -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    var language by remember { mutableStateOf("es") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.auth_setup_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        

        
        Text(stringResource(R.string.auth_setup_select_language), style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = language == "es",
                onClick = { language = "es" },
                label = { Text("Español") },
                leadingIcon = if (language == "es") { { Icon(Icons.Filled.Check, null) } } else null
            )
            FilterChip(
                selected = language == "en",
                onClick = { language = "en" },
                label = { Text("English") },
                leadingIcon = if (language == "en") { { Icon(Icons.Filled.Check, null) } } else null
            )
            FilterChip(
                selected = language == "ca",
                onClick = { language = "ca" },
                label = { Text("Català") },
                leadingIcon = if (language == "ca") { { Icon(Icons.Filled.Check, null) } } else null
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = stringResource(R.string.auth_setup_language_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirm(language) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.auth_setup_enter_securely), style = MaterialTheme.typography.titleMedium)
        }
    }
}


