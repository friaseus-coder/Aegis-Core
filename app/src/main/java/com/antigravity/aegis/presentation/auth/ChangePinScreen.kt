package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock

@Composable
fun ChangePinScreen(
    onPinChanged: () -> Unit
) {
    val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    
    // Auto-navigate if state becomes Authenticated (assuming changePin sets it)
    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            onPinChanged()
        }
    }

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cambiar PIN", style = MaterialTheme.typography.titleLarge)
        Text("Establece un nuevo PIN para tu cuenta", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 6) pin = it 
                error = null
            },
            label = { Text("Nuevo PIN") },
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showPin = !showPin }) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { 
                if (it.length <= 6) confirmPin = it 
                error = null
            },
            label = { Text("Confirmar PIN") },
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )
        
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (pin.length < 4) {
                    error = "El PIN debe tener al menos 4 dígitos"
                } else if (pin != confirmPin) {
                    error = "Los PINs no coinciden"
                } else {
                    viewModel.changePin(pin)
                }
            },
            enabled = pin.length >= 4 && confirmPin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
             Text("Cambiar y Entrar")
        }
    }
}
