package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Download

@Composable
fun LoginScreen(
    onNavigateToCreateUser: () -> Unit,
    onNavigateToImport: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    val loginErrorUi by viewModel.loginError.collectAsState()
    val loginError = loginErrorUi?.asString(LocalContext.current)
    
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.NeedsSetup -> {
                if (!hasNavigated) {
                    hasNavigated = true
                    onNavigateToCreateUser() // Now redirects to the Setup/Vault Init screen
                }
            }
            AuthState.Authenticated -> {
                if (!hasNavigated) {
                    hasNavigated = true
                    onLoginSuccess()
                }
            }
            else -> {}
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (authState) {
            AuthState.Loading, AuthState.NeedsSetup -> {
                 Box(
                     modifier = Modifier.fillMaxSize(),
                     contentAlignment = Alignment.Center
                 ) {
                     CircularProgressIndicator() // Wait until navigation happens
                 }
            }
            AuthState.Locked -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(160.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    if (loginError != null) {
                        Text(
                            text = loginError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    Button(
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.auth_login_enter_button), style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    

                    
                    TextButton(onClick = onNavigateToImport) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.auth_login_import_backup_button))
                    }
                }
            }
            else -> {}
        }
    }
}
