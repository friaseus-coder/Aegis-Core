package com.antigravity.aegis.presentation.timecontrol

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.R
import java.util.Calendar
import java.util.Date
import com.antigravity.aegis.presentation.components.AegisTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeControlScreen(
    viewModel: TimeControlViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val availableProjects by viewModel.availableProjects.collectAsState()
    val hours by viewModel.hours.collectAsState()
    val description by viewModel.description.collectAsState()
    val showStatusDialog by viewModel.showStatusDialog.collectAsState()

    // Date Utilities
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            viewModel.onDateSelected(cal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper for Future check (same logic as VM roughly)
    val isFuture = viewModel.isFutureDay(selectedDate)

    Scaffold(
        topBar = { AegisTopAppBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Control Horario",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Date Selection
            // Date Selection
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = android.text.format.DateFormat.getMediumDateFormat(context).format(Date(selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // Clickable modifier handles click
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                // Overlay clickable for Date Picker
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePickerDialog.show() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Project Selection (Dropdown)
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    value = selectedProject?.name ?: "",
                    onValueChange = {},
                    label = { Text("Proyecto / Subproyecto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableProjects.forEach { project ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(project.name, style = MaterialTheme.typography.bodyLarge)
                                    if (project.parentProjectId != null) {
                                        Text("Subproyecto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            },
                            onClick = {
                                viewModel.onProjectSelected(project)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                        HorizontalDivider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Content based on Time (Past vs Future)
            if (isFuture) {
                // Future: Planning
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Planificación Futura", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Estás seleccionando una fecha futura. Puedes agendar este trabajo directamente en tu calendario.")
                    }
                }
            } else {
                // Past/Today: Reporting
                OutlinedTextField(
                    value = hours,
                    onValueChange = { viewModel.onHoursChanged(it) },
                    label = { Text("Horas Trabajadas") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text(if (isFuture) "Descripción del Trabajo a Realizar" else "Descripción del Trabajo Realizado") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.onSaveClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedProject != null && (isFuture || hours.isNotBlank())
            ) {
                Text(if (isFuture) "Agendar en Calendario" else "Guardar Parte de Trabajo")
            }
        }
        
        if (showStatusDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissStatusDialog() },
                title = { Text("Proyecto No Activo") },
                text = { Text("El proyecto seleccionado no está Activo. ¿Deseas activarlo para imputar horas?") },
                confirmButton = {
                    Button(onClick = { viewModel.onConfirmStatusChange() }) {
                        Text("Activar y Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissStatusDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
