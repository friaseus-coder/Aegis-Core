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
    onNavigateToCreateReport: (Int) -> Unit,
    onNavigateToEditBudget: (Int, Int) -> Unit // projectId, quoteId
) {
    val project by viewModel.selectedProject.collectAsState()
    val tasks by viewModel.projectTasks.collectAsState()
    val reports by viewModel.projectReports.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    if (project == null) {
        Text(stringResource(R.string.crm_project_not_found))
        return
    }

    Scaffold(
        topBar = {
            AegisTopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.crm_task_add_button))
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
            
            Text(stringResource(R.string.dashboard_status_label, project!!.status), style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            


            // -- FINANCIAL SUMMARY --
            val financialSummary by viewModel.financialSummary.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.quotes_financial_summary_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 1: Income & Total Expenses
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         Column {
                            Text(stringResource(R.string.quotes_summary_income), style = MaterialTheme.typography.bodySmall)
                            Text("€${"%.2f".format(financialSummary.totalIncome)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.quotes_summary_income), style = MaterialTheme.typography.bodySmall)
                            Text("€${"%.2f".format(financialSummary.totalExpenses)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Row 2: Breakdown
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Gastos Directos", style = MaterialTheme.typography.bodySmall)
                            Text("€${"%.2f".format(financialSummary.directExpenses)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Gastos Generales (Imputados)", style = MaterialTheme.typography.bodySmall)
                            Text("€${"%.2f".format(financialSummary.allocatedGeneralExpenses)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Row 3: Profit & Ratios
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(R.string.quotes_summary_profit), style = MaterialTheme.typography.bodySmall)
                            Text(
                                "€${"%.2f".format(financialSummary.netProfit)}", 
                                style = MaterialTheme.typography.titleMedium, 
                                color = if (financialSummary.netProfit >= 0) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                        }
                         Column(horizontalAlignment = Alignment.End) {
                            Text("Beneficio/Hora", style = MaterialTheme.typography.bodySmall)
                            Text("€${"%.2f".format(financialSummary.profitPerHour)}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                     Text(stringResource(R.string.quotes_summary_margin) + ": ${"%.1f".format(financialSummary.margin)}%", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.End))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onNavigateToCreateReport(project!!.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.crm_report_create_button))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.crm_reports_section_title), style = MaterialTheme.typography.titleLarge)
// ...
            // -- BUDGETS SECTION --
            val budgets by viewModel.projectBudgets.collectAsState()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_module_budgets_title), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { onNavigateToEditBudget(project!!.id, 0) }) {
                    Text(stringResource(R.string.quotes_list_new_tab))
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                 items(budgets) { budget ->
                     ListItem(
                         headlineContent = { Text(budget.title) },
                         supportingContent = { Text("Total: €${budget.totalAmount} - ${budget.status}") },
                         trailingContent = {
                             TextButton(onClick = { onNavigateToEditBudget(0, budget.id) }) { Text(stringResource(R.string.quotes_list_view_tab)) }
                         }
                     )
                     HorizontalDivider()
                 }
                 if (budgets.isEmpty()) {
                     item { Text(stringResource(R.string.quotes_list_empty)) }
                 }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
             // -- EXPENSES SECTION --
            val expenses by viewModel.projectExpenses.collectAsState()
            Text(stringResource(R.string.home_module_expenses_title), style = MaterialTheme.typography.titleLarge)
             LazyColumn(modifier = Modifier.weight(1f)) {
                 items(expenses) { expense ->
                     ListItem(
                         headlineContent = { Text(expense.category) },
                         supportingContent = { Text(java.util.Date(expense.date).toString()) },
                         trailingContent = { Text("-€${expense.totalAmount}", color = MaterialTheme.colorScheme.error) }
                     )
                     HorizontalDivider()
                 }
                 if (expenses.isEmpty()) {
                     item { Text(stringResource(R.string.expenses_empty)) }
                 }
            }
             
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.crm_tasks_section_title), style = MaterialTheme.typography.titleLarge)
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
        title = { Text(stringResource(R.string.crm_task_new_dialog_title)) },
        text = {
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text(stringResource(R.string.crm_task_description_label)) }
            )
        },
        confirmButton = {
            Button(onClick = { 
                if (description.isNotBlank()) onConfirm(description) 
            }) {
                Text(stringResource(R.string.general_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}
