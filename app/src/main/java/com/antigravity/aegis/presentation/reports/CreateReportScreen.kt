package com.antigravity.aegis.presentation.reports

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    projectId: Int,
    onSaveReport: (String, android.graphics.Bitmap?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var signatureView: SignatureView? = null
    val context = LocalContext.current

    // Simple Camera Logic (Just scaffolding the Intent, real file handling needs strict URI setup)
    // For this prototype, we'll simulate "Photo Taken"
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // In a real app we'd save this bitmap to file and get URI
            // or use TakePicture with a content URI.
            // For now, let's just toast
            Toast.makeText(context, context.getString(R.string.field_report_photo_captured), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.field_report_create_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.general_cancel)) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.field_report_desc_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes available space
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.field_report_attach_photo_button))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.field_report_signature_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Signature Area
            // ... (Signature pad implementation would go here)
            // For now, a placeholder box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(androidx.compose.ui.graphics.Color.LightGray)
            ) {
                 Text("Signature Pad Placeholder", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
            
            TextButton(
                onClick = { signatureView?.clear() },
                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
            ) {
                Text(stringResource(R.string.field_report_clear_signature))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (description.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.field_report_desc_required), Toast.LENGTH_SHORT).show()
                    } else {
                        val bitmap = signatureView?.getSignatureBitmap()
                        onSaveReport(description, bitmap)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.field_report_finish_button))
            }
        }
    }
}
