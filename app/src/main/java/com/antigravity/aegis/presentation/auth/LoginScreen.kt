package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.locked_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            label = { Text(stringResource(R.string.enter_pin_label)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onLogin(pin) },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.unlock_button))
        }
    }
}
