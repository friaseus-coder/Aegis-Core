package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecoveryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangePin: () -> Unit
) {
    val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    
    // Recovery Step: 0 = Words, 1 = Email/Phone
    var step by remember { mutableIntStateOf(0) }
    
    // Inputs
    var word1 by remember { mutableStateOf("") }
    var word2 by remember { mutableStateOf("") }
    
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Handle Navigation on Success
    LaunchedEffect(authState) {
        if (authState == AuthState.RecoverySuccess) {
            onNavigateToChangePin()
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
        Text("Recuperación de PIN", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (step == 0) {
            // STEP 1: WORDS
            Text("Introduce tus 2 palabras secretas:")
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = word1,
                onValueChange = { word1 = it },
                label = { Text("Palabra 1") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
             OutlinedTextField(
                value = word2,
                onValueChange = { word2 = it },
                label = { Text("Palabra 2") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.recoverWithWords(listOf(word1, word2)) },
                enabled = word1.isNotEmpty() && word2.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recuperar")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { step = 1 }) {
                Text("Probar con Email y Teléfono")
            }
        } else {
            // STEP 2: EMAIL/PHONE
            Text("Introduce tu Email y Teléfono asociados:")
             Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
             OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                singleLine = true
            )
            
             Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.recoverWithEmailPhone(email, phone) },
                enabled = email.isNotEmpty() && phone.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recuperar con Email")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { step = 0 }) {
                Text("Volver a Palabras")
            }
        }
        
        if (loginError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(loginError ?: "", color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Cancelar")
        }
    }
}
