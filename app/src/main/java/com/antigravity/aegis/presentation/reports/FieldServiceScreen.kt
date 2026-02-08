package com.antigravity.aegis.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.crm.CrmViewModel
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldServiceScreen(
    viewModel: CrmViewModel,
    onNavigateToCreateReport: (Int) -> Unit = {}
) {
    val allReports by viewModel.allWorkReports.collectAsState()

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // For simplicity, use projectId 1. 
                    // In a real app, you might show a project picker dialog
                    onNavigateToCreateReport(1)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_add_report))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Screen Title
            Text(
                text = stringResource(R.string.field_service_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                stringResource(R.string.crm_reports_section_title), 
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(allReports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.crm_report_item_title, report.id), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.dashboard_deadline_label, java.util.Date(report.date).toString())) // Reusing deadline label for date format temporarily or adding general_date
                            Text(stringResource(R.string.crm_project_id_label, report.projectId))
                            Text(report.description, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
                if (allReports.isEmpty()) {
                    item { Text(stringResource(R.string.field_reports_not_found)) }
                }
            }
        }
    }
}
