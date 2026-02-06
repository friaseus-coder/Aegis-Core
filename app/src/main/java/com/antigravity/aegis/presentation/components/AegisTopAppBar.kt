package com.antigravity.aegis.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.ui.theme.LocalCompanyLogoUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null
) {
    val logoUri = LocalCompanyLogoUri.current
    
    TopAppBar(
        modifier = modifier,
        title = { 
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BovedaLogo(logoUri = logoUri, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Aegis Core", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            // Screen-specific actions
            actions()
            
            // Standard Settings Button (always at the end if action provided)
            if (onNavigateToSettings != null) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
