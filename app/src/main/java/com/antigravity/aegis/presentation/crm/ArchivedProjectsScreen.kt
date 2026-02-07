package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedProjectsScreen(
    viewModel: CrmViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Assuming viewModel has functionality to get archived projects
    // We might need to add getArchivedProjects to ViewModel or filter
    // For now, let's assume we add `archivedProjects` flow to ViewModel
    val archivedProjects by viewModel.archivedProjects.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AegisTopAppBar(title = stringResource(R.string.crm_archived_projects_title))
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (archivedProjects.isEmpty()) {
                Text(stringResource(R.string.crm_archived_projects_empty))
            } else {
                LazyColumn {
                    items(archivedProjects) { project ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // Grayscale-ish check manually or use color filter
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Archived on: ${java.util.Date(project.endDate ?: 0).toString()}", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = { viewModel.reactivateProject(project.id) }) {
                                    Text(stringResource(R.string.crm_project_reactivate_button))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
