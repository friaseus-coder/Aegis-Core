package com.antigravity.aegis.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun TransferStatusDialog(
    state: Any, // Generic state wrapper, typically TransferState sealed class
    onDismiss: () -> Unit,
    onConfirmWipe: (Boolean) -> Unit
) {
    // We assume state has properties: Loading, Error, ValidationSuccess (uri), etc.
    // Using reflection or casting for generic usage is tricky without common interface.
    // For now, I will create a specific Composable for the CrmViewModel.TransferState structure
    // but placed here for reference or copy-paste.
}

// Actually, let's make it data-driven
@Composable
fun ImportConfirmationDialog(
    onConfirm: (Boolean) -> Unit, // True = Wipe, False = Append
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import Options") },
        text = { Text("Do you want to DELETE existing data before importing?\n\nYES: A backup will be created, data cleared, and new data imported.\nNO: New data will be added to existing list.") },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) { Text("YES (Wipe & Import)") }
        },
        dismissButton = {
             TextButton(onClick = { onConfirm(false) }) { Text("NO (Append)") }
        }
    )
}
