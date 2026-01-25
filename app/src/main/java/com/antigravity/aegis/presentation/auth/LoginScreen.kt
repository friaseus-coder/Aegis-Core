package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.data.model.UserEntity
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.appcompat.app.AppCompatActivity
import com.antigravity.aegis.data.security.BiometricPromptManager

import java.util.Locale
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    onNavigateToCreateUser: () -> Unit
) {
    val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val users by viewModel.users.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val language by viewModel.language.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val isBiometricAvailable by viewModel.isBiometricAvailable.collectAsState()
    val biometricPromptState by viewModel.biometricPromptState.collectAsState()
    
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val biometricPromptManager = remember(activity) {
        if (activity != null) BiometricPromptManager(activity) else null
    }

    LaunchedEffect(biometricPromptState) {
        biometricPromptState?.let { config ->
            biometricPromptManager?.showBiometricPrompt(
                title = context.getString(config.titleResId),
                description = context.getString(config.descriptionResId),
                cryptoObject = config.cryptoObject
            )
            viewModel.onBiometricPromptShown()
        }
    }

    LaunchedEffect(biometricPromptManager) {
        biometricPromptManager?.promptResults?.collect { result ->
            viewModel.onBiometricResult(result)
        }
    }
    
    // Track if we've already navigated to avoid duplicate navigation
    var hasNavigated by remember { mutableStateOf(false) }
    
    // Safe auto-redirect based on authState
    LaunchedEffect(authState) {
        when {
            authState == AuthState.NeedsSetup && !hasNavigated -> {
                hasNavigated = true
                onNavigateToCreateUser()
            }
            authState == AuthState.Authenticated && !hasNavigated -> {
                hasNavigated = true
                onLogin("") // Empty string, navigation handle is the callback's purpose now
            }
        }
    }

    var pin by rememberSaveable { mutableStateOf("") }

    // Main content based on auth state - key forces recomposition when language changes
    key(language) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (authState) {
                // ... (Loading/NeedsSetup) ...
                AuthState.Loading -> {
                     Box(
                         modifier = Modifier.fillMaxSize(),
                         contentAlignment = Alignment.Center
                     ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             CircularProgressIndicator()
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(stringResource(R.string.loading_label))
                         }
                     }
                }
                AuthState.NeedsSetup -> {
                    Box(
                         modifier = Modifier.fillMaxSize(),
                         contentAlignment = Alignment.Center
                     ) {
                         CircularProgressIndicator()
                     }
                }
                AuthState.Locked, AuthState.Authenticated -> {
                    val loginError by viewModel.loginError.collectAsState()
                    
                    // Clear PIN when there's an error
                    LaunchedEffect(loginError) {
                        if (loginError != null) {
                            pin = ""
                        }
                    }
                    
                    // Show login UI
                    LoginContent(
                        users = users,
                        selectedUser = selectedUser,
                        language = language,
                        pin = pin,
                        loginError = loginError,
                        isBiometricAvailable = isBiometricAvailable,
                        isAuthenticated = authState == AuthState.Authenticated,
                        onPinChange = { 
                            if (it.length <= 6) {
                                pin = it
                                viewModel.clearLoginError()
                            }
                        },
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onSelectUser = { viewModel.selectUser(it) },
                        onNavigateToCreateUser = onNavigateToCreateUser,
                        onLogin = { viewModel.login(pin) },
                        onBiometricLogin = { viewModel.loginBiometric() },
                        onEnableBiometric = { viewModel.enableBiometric() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    users: List<UserEntity>,
    selectedUser: UserEntity?,
    language: String,
    pin: String,
    loginError: String?,
    isBiometricAvailable: Boolean,
    isAuthenticated: Boolean,
    onPinChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSelectUser: (UserEntity) -> Unit,
    onNavigateToCreateUser: () -> Unit,
    onLogin: () -> Unit,
    onBiometricLogin: () -> Unit,
    onEnableBiometric: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language Toggle and Create User
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateToCreateUser) {
                Text(stringResource(R.string.create_user_label), fontSize = 12.sp)
            }
            
            Row {
                TextButton(onClick = { onLanguageChange("es") }) {
                    Text("ES", fontWeight = if (language == "es") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal)
                }
                TextButton(onClick = { onLanguageChange("en") }) {
                    Text("EN", fontWeight = if (language == "en") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Icon
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User Selector
        Text(stringResource(R.string.select_user_label), style = MaterialTheme.typography.labelMedium)
        
        if (users.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user, 
                        isSelected = user == selectedUser,
                        onClick = { onSelectUser(user) }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("No users found", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        var showPin by remember { mutableStateOf(false) }
        
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = { Text(stringResource(R.string.enter_pin_label)) },
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = loginError != null,
            supportingText = {
                if (loginError != null) {
                    Text(loginError, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { showPin = !showPin }) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = if (showPin) "Ocultar PIN" else "Mostrar PIN"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onLogin,
            enabled = pin.length >= 4 && selectedUser != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.unlock_button))
        }

        if (isBiometricAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBiometricLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.biometric_login_button))
            }
        }
        
        // Enable Biometric Button - Only show after login when MK is in memory
        if (isAuthenticated && !isBiometricAvailable && selectedUser != null) {
             Spacer(modifier = Modifier.height(8.dp))
             TextButton(onClick = onEnableBiometric) {
                 Text(stringResource(R.string.enable_biometrics_button), fontSize = 12.sp)
             }
        }
    }
}

@Composable
fun UserCard(user: UserEntity, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.size(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(user.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
