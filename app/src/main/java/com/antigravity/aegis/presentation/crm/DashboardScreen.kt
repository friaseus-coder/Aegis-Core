package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CrmViewModel,
    onNavigateToClients: () -> Unit,
    onNavigateToProject: (Int) -> Unit
) {
    val activeProjects by viewModel.activeProjects.collectAsState()

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            // Optional: Quick add project?
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Screen Title
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Button(
                onClick = onNavigateToClients,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.manage_clients))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.active_projects),
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeProjects) { project ->
                    Card(
                        onClick = { onNavigateToProject(project.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(project.name, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.status_label, project.status), style = MaterialTheme.typography.bodyMedium)
                            project.endDate?.let {
                                Text(stringResource(R.string.deadline_label, it), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                
                if (activeProjects.isEmpty()) {
                    item {
                        Text(stringResource(R.string.no_active_projects), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
