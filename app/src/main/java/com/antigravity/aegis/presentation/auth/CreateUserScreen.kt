package com.antigravity.aegis.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(
    onUserCreated: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    // Ideally use a dropdown, for now simple toggle or 2 buttons
    var language by remember { mutableStateOf("es") } 

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.create_new_user_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(stringResource(R.string.language_label), style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChip(
                selected = language == "es",
                onClick = { language = "es" },
                label = { Text(stringResource(R.string.language_spanish)) }
            )
            FilterChip(
                selected = language == "en",
                onClick = { language = "en" },
                label = { Text(stringResource(R.string.language_english)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            label = { Text(stringResource(R.string.enter_pin_label)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
             modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onUserCreated(name, language, pin) },
            enabled = name.isNotBlank() && pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_and_enter))
        }
    }
}
