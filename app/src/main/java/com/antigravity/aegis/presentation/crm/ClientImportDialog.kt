package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R

@Composable
fun ClientImportDialog(
    onDismiss: () -> Unit,
    onDownloadTemplate: () -> Unit,
    onUploadFile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_clients_import_dialog_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onDownloadTemplate()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.crm_clients_import_option_template))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onUploadFile()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.crm_clients_import_option_upload))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.general_cancel))
            }
        }
    )
}

@Composable
fun ClientImportConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var selectedModeReplace by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_clients_import_dialog_title)) },
        text = {
            Column {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = !selectedModeReplace,
                        onClick = { selectedModeReplace = false }
                    )
                    Text(
                        stringResource(R.string.crm_clients_import_mode_add),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedModeReplace,
                        onClick = { selectedModeReplace = true }
                    )
                    Text(
                        stringResource(R.string.crm_clients_import_mode_replace),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (selectedModeReplace) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.crm_clients_import_warning_replace),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedModeReplace) }) {
                Text(stringResource(R.string.general_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.general_cancel))
            }
        }
    )
}
