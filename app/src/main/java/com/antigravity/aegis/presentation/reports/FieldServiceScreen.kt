package com.antigravity.aegis.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.presentation.crm.CrmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldServiceScreen(
    viewModel: CrmViewModel
) {
    val allReports by viewModel.allWorkReports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Field Service Console") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                "All Work Reports", 
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(allReports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Report #${report.id}", style = MaterialTheme.typography.titleMedium)
                            Text("Date: ${java.util.Date(report.date)}")
                            Text("Project ID: ${report.projectId}") // Could join name if needed, keeping simple
                            Text(report.description, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
                if (allReports.isEmpty()) {
                    item { Text("No work reports found.") }
                }
            }
        }
    }
}
