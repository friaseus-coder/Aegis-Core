package com.antigravity.aegis.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.antigravity.aegis.data.model.ActiveRole
import com.antigravity.aegis.data.model.EntityType
import com.antigravity.aegis.presentation.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileConfigScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val userConfig by viewModel.userConfig.collectAsState()
    
    // State buffer for editing
    var name by remember { mutableStateOf("") }
    var entityType by remember { mutableStateOf(EntityType.PARTICULAR) }
    var activeRole by remember { mutableStateOf(ActiveRole.AUTONOMO) }
    var isDualMode by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImagePath by remember { mutableStateOf<String?>(null) }
    
    // Initialize state from config once loaded
    LaunchedEffect(userConfig) {
        userConfig?.let {
            name = it.titularName
            entityType = it.entityType
            activeRole = it.activeRole
            isDualMode = it.isDualModeEnabled
            currentImagePath = it.profileImageUri
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Image Picker Section
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(selectedImageUri)
                                    .build()
                            ),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!currentImagePath.isNullOrEmpty()) {
                         val file = File(currentImagePath)
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
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo")
                }
            }
            
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Titular") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            
            // Identity Type
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Identidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = entityType == EntityType.PARTICULAR,
                        onClick = { entityType = EntityType.PARTICULAR }
                    )
                    Text("Particular")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = entityType == EntityType.EMPRESA,
                        onClick = { entityType = EntityType.EMPRESA }
                    )
                    Text("Empresa")
                }
                if (entityType == EntityType.EMPRESA) {
                    Text(
                        "Se recomienda subir el Logo de la empresa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Divider()
            
            // Role Configuration
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Rol Predeterminado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Define cómo se comportará la app al iniciar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = activeRole == ActiveRole.AUTONOMO,
                        onClick = { activeRole = ActiveRole.AUTONOMO }
                    )
                    Column {
                        Text("Autónomo")
                        Text("Acceso total (Gestión)", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = activeRole == ActiveRole.TRABAJADOR,
                        onClick = { activeRole = ActiveRole.TRABAJADOR }
                    )
                     Column {
                        Text("Trabajador")
                        Text("Solo módulos operativos", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Divider()
            
            // Dual Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Column(modifier = Modifier.weight(1f)) {
                    Text("Modo Flexible (Dual)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Permite alternar entre vista Gestión y Operativa desde el Dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                 }
                 Switch(
                     checked = isDualMode,
                     onCheckedChange = { isDualMode = it }
                 )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.updateProfile(
                        name,
                        entityType,
                        activeRole,
                        isDualMode,
                        selectedImageUri
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
