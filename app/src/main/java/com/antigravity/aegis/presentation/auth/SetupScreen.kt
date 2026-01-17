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

@Composable
fun SetupScreen(
    state: SetupUiState,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    // In a real app, this would be a multi-step wizard:
    // 1. Show Welcome
    // 2. Show Seed Phrase (Force write down)
    // 3. Create PIN
    // 4. Confirm PIN
    
    // For this demo, we show everything in one scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.seedPhrase) { index, word ->
                SeedWordChip(index + 1, word)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = stringResource(R.string.create_pin_label))
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            label = { Text(stringResource(R.string.pin_hint)) },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onConfirm(pin) },
            enabled = pin.length == 6,
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
