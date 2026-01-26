package com.antigravity.aegis.presentation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.antigravity.aegis.data.model.ActiveRole
import com.antigravity.aegis.data.model.EntityType
import com.antigravity.aegis.data.model.UserConfig
import java.io.File

@Composable
fun UserHeader(
    userConfig: UserConfig?,
    onNavigateToProfile: () -> Unit,
    onToggleRole: (ActiveRole) -> Unit,
    modifier: Modifier = Modifier
) {
    if (userConfig == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToProfile),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!userConfig.profileImageUri.isNullOrEmpty()) {
                    val file = File(userConfig.profileImageUri)
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(file)
                                .build()
                        ),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userConfig.titularName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Entity Type Badge (Empresa/Particular)
                Surface(
                    color = if (userConfig.entityType == EntityType.EMPRESA) 
                        MaterialTheme.colorScheme.secondaryContainer 
                    else 
                        MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (userConfig.entityType == EntityType.EMPRESA) "EMPRESA" else "PARTICULAR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (userConfig.entityType == EntityType.EMPRESA) 
                            MaterialTheme.colorScheme.onSecondaryContainer 
                        else 
                            MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                // Role Badge (Autónomo/Trabajador)
                Surface(
                    color = if (userConfig.activeRole == ActiveRole.AUTONOMO) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (userConfig.activeRole == ActiveRole.AUTONOMO) "AUTÓNOMO" else "TRABAJADOR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Role Switcher (Dual Mode)
            if (userConfig.isDualModeEnabled) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Switch(
                        checked = userConfig.activeRole == ActiveRole.AUTONOMO,
                        onCheckedChange = { isAutonomo ->
                            onToggleRole(if (isAutonomo) ActiveRole.AUTONOMO else ActiveRole.TRABAJADOR)
                        },
                         thumbContent = {
                            if (userConfig.activeRole == ActiveRole.AUTONOMO) {
                                // Icon for Autonomo (e.g. Admin/Manager)
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp))
                            } else {
                                // Icon for Worker
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }
                    )
                    Text(
                        text = if (userConfig.activeRole == ActiveRole.AUTONOMO) "Gestión" else "Operativa",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
