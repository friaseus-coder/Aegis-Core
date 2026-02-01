package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.BovedaLogo
import com.antigravity.aegis.ui.theme.LocalCompanyLogoUri
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    viewModel: CrmViewModel,
    onNavigateToCreateReport: (Int) -> Unit
) {
    val project by viewModel.selectedProject.collectAsState()
    val tasks by viewModel.projectTasks.collectAsState()
    val reports by viewModel.projectReports.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    if (project == null) {
        Text(stringResource(R.string.project_not_found))
        return
    }

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task_fab))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Project Name as Header
            Text(
                text = project!!.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(stringResource(R.string.status_label, project!!.status), style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onNavigateToCreateReport(project!!.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.create_work_report_button))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.work_reports_section_title), style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                 items(reports) { report ->
                     ListItem(
                         headlineContent = { Text("Report #${report.id}") },
                         supportingContent = { Text(java.util.Date(report.date).toString()) }
                     )
                     Divider()
                 }
                 if (reports.isEmpty()) {
                     item { Text("No reports yet.") }
                 }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.tasks_section_title), style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { isChecked ->
                                viewModel.updateTaskStatus(task, isChecked)
                            }
                        )
                        Text(
                            text = task.description,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Divider()
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { desc ->
                    viewModel.createTask(project!!.id, desc)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_task_dialog_title)) },
        text = {
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text(stringResource(R.string.task_description_label)) }
            )
        },
        confirmButton = {
            Button(onClick = { 
                if (description.isNotBlank()) onConfirm(description) 
            }) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}
