package com.antigravity.aegis.presentation.crm

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.BovedaLogo
import com.antigravity.aegis.ui.theme.LocalCompanyLogoUri
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import com.antigravity.aegis.presentation.components.AegisTopAppBar
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.collectLatest
import com.antigravity.aegis.presentation.util.UiText
import com.antigravity.aegis.domain.util.onSuccess
import com.antigravity.aegis.domain.util.onError
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    viewModel: CrmViewModel,
    onNavigateToKanban: () -> Unit,
    onNavigateToEditBudget: (Int, Int) -> Unit, // projectId, quoteId
    onNavigateBack: () -> Unit = {}
) {
    val project by viewModel.selectedProject.collectAsState()
    val subProjects by viewModel.subProjects.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    var showAddSubProjectDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var subProjectToDelete by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    var subProjectToEdit by remember { mutableStateOf<com.antigravity.aegis.data.local.entity.ProjectEntity?>(null) }
    val context = LocalContext.current

    var showCloseConfirmDialog by remember { mutableStateOf(false) }

    // Observar el evento de compartir PDF
    LaunchedEffect(Unit) {
        viewModel.pdfShareEvent.collectLatest { pdfUri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(intent, "Compartir presupuesto PDF").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    if (project == null) {
        Text(stringResource(R.string.crm_project_not_found))
        return
    }

    Scaffold(
        topBar = {
            AegisTopAppBar(
                 actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.general_options))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.crm_project_save_as_template)) },
                            onClick = {
                                expanded = false
                                showSaveTemplateDialog = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Borrar proyecto", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = {
                                expanded = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ─ CABECERA: Nombre y estado del proyecto ─
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.dashboard_status_label, project!!.status),
                            style = MaterialTheme.typography.titleMedium
                        )
                        project!!.description?.let {
                            if (it.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // --- BOTÓN CERRAR PROYECTO ---
                    if (project!!.status == CrmStatus.WON) {
                        Button(
                            onClick = { showCloseConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(stringResource(R.string.crm_project_detail_btn_close), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    } else if (project!!.status == CrmStatus.PROJECT_CLOSED) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.crm_project_status_closed),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ─ SUBPROYECTOS ─
            if (project!!.parentProjectId == null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.crm_project_subprojects_title), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { showAddSubProjectDialog = true }) {
                            Text(stringResource(R.string.general_add))
                        }
                    }
                }

                if (subProjects.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.crm_subproject_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(subProjects, key = { it.id }) { sub ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sub.name, style = MaterialTheme.typography.bodyLarge)
                                        if (sub.price != null) {
                                            Text(
                                                stringResource(R.string.crm_subproject_price_prefix, currencySymbol + "%.2f".format(sub.price)),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (sub.estimatedTime != null && sub.estimatedTimeUnit != null) {
                                            Text(stringResource(R.string.crm_subproject_time_prefix, sub.estimatedTime.toString(), sub.estimatedTimeUnit), style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (!sub.materials.isNullOrBlank()) {
                                            Text(stringResource(R.string.crm_subproject_materials_prefix, sub.materials), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { subProjectToEdit = sub }) {
                                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.general_edit), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { subProjectToDelete = sub }) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.general_delete), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Total subproyectos
                val totalSubs = subProjects.sumOf { it.price ?: 0.0 }
                if (totalSubs > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "Total presupuestado: $currencySymbol%.2f".format(totalSubs),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ─ RESUMEN FINANCIERO ─
            item {
                val financialSummary by viewModel.financialSummary.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.quotes_financial_summary_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(stringResource(R.string.quotes_summary_income), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencySymbol + "%.2f".format(financialSummary.totalIncome),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(R.string.home_module_expenses_title), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencySymbol + "%.2f".format(financialSummary.totalExpenses),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(stringResource(R.string.financial_direct_expenses), style = MaterialTheme.typography.bodySmall)
                                Text(currencySymbol + "%.2f".format(financialSummary.directExpenses), style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(R.string.financial_allocated_general), style = MaterialTheme.typography.bodySmall)
                                Text(currencySymbol + "%.2f".format(financialSummary.allocatedGeneralExpenses), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(stringResource(R.string.quotes_summary_profit), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencySymbol + "%.2f".format(financialSummary.netProfit),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (financialSummary.netProfit >= 0) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                stringResource(R.string.quotes_summary_margin) + ": ${"%.1f".format(financialSummary.margin)}%",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // ─ PRESUPUESTOS ─
            item {
                val budgets by viewModel.projectBudgets.collectAsState()
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.home_module_budgets_title), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { onNavigateToKanban() }) {
                            Text(stringResource(R.string.quotes_list_view_tab))
                        }
                    }
                    if (budgets.isEmpty()) {
                        Text(stringResource(R.string.quotes_list_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        budgets.forEach { budget ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                ListItem(
                                    headlineContent = { Text(budget.title) },
                                    supportingContent = { Text(stringResource(R.string.financial_total, budget.totalAmount, budget.status)) },
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { viewModel.generateAndShareQuotePdf(budget.id) }) {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = "PDF",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            TextButton(onClick = { onNavigateToEditBudget(0, budget.id) }) {
                                                Text(stringResource(R.string.quotes_list_view_tab))
                                            }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // ─ GASTOS ─
            item {
                val expenses by viewModel.projectExpenses.collectAsState()
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.home_module_expenses_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (expenses.isEmpty()) {
                        Text(stringResource(R.string.expenses_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        expenses.forEach { expense ->
                            ListItem(
                                headlineContent = { Text(expense.category) },
                                supportingContent = {
                                    Text(
                                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                            .format(java.util.Date(expense.date))
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        "-" + currencySymbol + "%.2f".format(expense.totalAmount),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // ─ SESIONES ─
            item {
                val sessions by viewModel.projectSessions.collectAsState()
                var showAddSessionDialog by remember { mutableStateOf(false) }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.crm_session_title), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { showAddSessionDialog = true }) {
                            Text(stringResource(R.string.crm_session_add_btn))
                        }
                    }

                    if (sessions.isEmpty()) {
                        Text(
                            stringResource(R.string.crm_session_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        sessions.forEach { session ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.date)),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(session.duration, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Text(session.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(stringResource(R.string.crm_session_notes) + ":", style = MaterialTheme.typography.labelSmall)
                                    Text(session.notes, style = MaterialTheme.typography.bodyMedium)
                                    if (session.exercises.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(stringResource(R.string.crm_session_exercises) + ":", style = MaterialTheme.typography.labelSmall)
                                        Text(session.exercises, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (session.nextSessionDate != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = MaterialTheme.shapes.extraSmall
                                        ) {
                                            Text(
                                                text = stringResource(R.string.crm_session_next_date) + ": " + 
                                                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.nextSessionDate)),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showAddSessionDialog) {
                        AddSessionDialog(
                            onDismiss = { showAddSessionDialog = false },
                            onConfirm = { date, loc, dur, notes, exercises, nextDate ->
                                viewModel.createSession(project!!.id, date, loc, dur, notes, exercises, nextDate)
                                showAddSessionDialog = false
                            }
                        )
                    }
                }
            }
        }

        if (showAddSubProjectDialog) {
             AddSubProjectDialog(
                onDismiss = { showAddSubProjectDialog = false },
                onConfirm = { name, materials, price, estTime, estUnit ->
                    viewModel.createSubProject(
                        parentProjectId = project!!.id, 
                        name = name, 
                        startDate = System.currentTimeMillis(),
                        materials = materials,
                        price = price,
                        estimatedTime = estTime,
                        estimatedTimeUnit = estUnit
                    )
                    showAddSubProjectDialog = false
                }
             )
        }

        // Diálogo de confirmación de borrado de SUBPROYECTO
        if (subProjectToDelete != null) {
            AlertDialog(
                onDismissRequest = { subProjectToDelete = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text(stringResource(R.string.crm_subproject_delete_title)) },
                text = { Text(stringResource(R.string.crm_subproject_delete_confirm, subProjectToDelete!!.name)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSubProject(subProjectToDelete!!.id)
                            subProjectToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.general_delete), color = MaterialTheme.colorScheme.onError) }
                },
                dismissButton = {
                    TextButton(onClick = { subProjectToDelete = null }) { Text(stringResource(R.string.general_cancel)) }
                }
            )
        }

        // Diálogo de edición de SUBPROYECTO
        if (subProjectToEdit != null) {
            AddSubProjectDialog(
                existing = subProjectToEdit,
                onDismiss = { subProjectToEdit = null },
                onConfirm = { name, materials, price, estTime, estUnit ->
                    viewModel.updateSubProject(
                        subProjectId = subProjectToEdit!!.id,
                        name = name,
                        materials = materials,
                        price = price,
                        estimatedTime = estTime,
                        estimatedTimeUnit = estUnit
                    )
                    subProjectToEdit = null
                }
            )
        }


        if (showSaveTemplateDialog) {
             SaveTemplateDialog(
                currentName = project!!.name,
                onDismiss = { showSaveTemplateDialog = false },
                onConfirm = { templateName, category ->
                    viewModel.saveAsTemplate(project!!.id, templateName, category)
                    showSaveTemplateDialog = false
                }
             )
        }

        // Diálogo de confirmación de borrado
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Borrar proyecto") },
                text = {
                    Text("Se borrará el proyecto \"${project!!.name}\" y todos sus presupuestos asociados en el CRM. Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteProjectWithQuotes(
                                projectId = project!!.id,
                                onSuccess = {
                                    viewModel.clearSelection()
                                    onNavigateBack()
                                },
                                onError = { errorMsg ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Error al borrar: $errorMsg",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Borrar", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo de confirmación de CIERRE DE PROYECTO
        if (showCloseConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCloseConfirmDialog = false },
                title = { Text(stringResource(R.string.crm_project_detail_btn_close)) },
                text = { Text("¿Estás seguro de que deseas cerrar permanentemente el proyecto \"${project!!.name}\"? Ya no podrá participar en el reparto de costes generales.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.closeProject(project!!.id) { result ->
                                result.onSuccess {
                                    showCloseConfirmDialog = false
                                }
                            }
                        }
                    ) { Text(stringResource(R.string.general_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseConfirmDialog = false }) {
                        Text(stringResource(R.string.general_cancel))
                    }
                }
            )
        }
    }
}
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubProjectDialog(
    existing: com.antigravity.aegis.data.local.entity.ProjectEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Double?, Double?, String?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var materials by remember { mutableStateOf(existing?.materials ?: "") }
    var priceStr by remember { mutableStateOf(existing?.price?.let { "%.2f".format(it) } ?: "") }
    var estimatedTimeStr by remember { mutableStateOf(existing?.estimatedTime?.let { "%.1f".format(it) } ?: "") }
    
    var expandedUnitList by remember { mutableStateOf(false) }
    
    val timeUnits = listOf(
        stringResource(R.string.crm_subproject_unit_hours), 
        stringResource(R.string.crm_subproject_unit_days), 
        stringResource(R.string.crm_subproject_unit_weeks)
    )
    var selectedTimeUnit by remember { mutableStateOf(existing?.estimatedTimeUnit ?: timeUnits[0]) }
    val dialogTitle = if (existing != null) stringResource(R.string.crm_subproject_edit_title) else stringResource(R.string.crm_subproject_new_title)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(stringResource(R.string.crm_subproject_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = materials,
                    onValueChange = { materials = it },
                    label = { Text(stringResource(R.string.crm_subproject_materials_label)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.') },
                    label = { Text(stringResource(R.string.crm_subproject_price_label)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estimatedTimeStr,
                        onValueChange = { estimatedTimeStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.') },
                        label = { Text(stringResource(R.string.crm_subproject_time_label)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedUnitList,
                        onExpandedChange = { expandedUnitList = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedTimeUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.crm_subproject_unit_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitList) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnitList,
                            onDismissRequest = { expandedUnitList = false }
                        ) {
                            timeUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedTimeUnit = unit
                                        expandedUnitList = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) {
                    val price = priceStr.toDoubleOrNull()
                    val estTime = estimatedTimeStr.toDoubleOrNull()
                    onConfirm(name, materials.takeIf { it.isNotBlank() }, price, estTime, selectedTimeUnit)
                }
            }) {
                Text(stringResource(R.string.general_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Long?) -> Unit) {
    var description by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_task_new_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text(stringResource(R.string.crm_task_description_label)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationHours, 
                    onValueChange = { durationHours = it.filter { c -> c.isDigit() } }, 
                    label = { Text(stringResource(R.string.crm_task_duration_hours_label)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (description.isNotBlank()) {
                    val duration = durationHours.toLongOrNull()?.let { it * 3600000 } // Hours to Ms
                    onConfirm(description, duration)
                }
            }) {
                Text(stringResource(R.string.general_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}
@Composable
fun SaveTemplateDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    val suffix = stringResource(R.string.crm_project_template_suffix)
    var name by remember { mutableStateOf(currentName + suffix) }
    var category by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_project_save_as_template)) },
        text = {
            Column {
                Text(stringResource(R.string.crm_project_save_template_desc))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(stringResource(R.string.crm_project_template_name_label)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Simple text field for category for now, or dropdown if we have list
                OutlinedTextField(
                    value = category, 
                    onValueChange = { category = it }, 
                    label = { Text(stringResource(R.string.crm_project_template_category_label)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) onConfirm(name, category.takeIf { it.isNotBlank() }) 
            }) {
                Text(stringResource(R.string.general_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String, String, Long?) -> Unit
) {
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var location by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf("") }
    var nextSessionDate by remember { mutableStateOf<Long?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showNextDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crm_session_add_btn)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.crm_session_date) + ": " + dateFormatter.format(java.util.Date(date)))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.crm_session_location)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(stringResource(R.string.crm_session_duration)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.crm_session_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = exercises,
                    onValueChange = { exercises = it },
                    label = { Text(stringResource(R.string.crm_session_exercises)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = nextSessionDate != null,
                        onCheckedChange = { 
                            if (it) nextSessionDate = System.currentTimeMillis() + 86400000 * 7 // +1 semana
                            else nextSessionDate = null 
                        }
                    )
                    Text(stringResource(R.string.crm_session_next_date))
                }
                
                if (nextSessionDate != null) {
                    OutlinedButton(
                        onClick = { showNextDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(dateFormatter.format(java.util.Date(nextSessionDate!!)))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(date, location, duration, notes, exercises, nextSessionDate) 
            }) {
                Text(stringResource(R.string.general_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showNextDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextSessionDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showNextDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { nextSessionDate = it }
                    showNextDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
