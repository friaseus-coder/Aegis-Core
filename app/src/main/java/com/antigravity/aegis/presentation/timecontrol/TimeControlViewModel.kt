package com.antigravity.aegis.presentation.timecontrol

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.ProjectStatus
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TimeControlViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- State ---
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject

    private val _hours = MutableStateFlow("")
    val hours: StateFlow<String> = _hours

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _showStatusDialog = MutableStateFlow(false)
    val showStatusDialog: StateFlow<Boolean> = _showStatusDialog
    
    private val _pendingSaveAction = MutableStateFlow<(() -> Unit)?>(null)

    // List of ALL projects (Roots and Subs) flattened for selection
    // We might want to group them in UI, but flat list is easier for Dropdown
    val availableProjects: StateFlow<List<ProjectEntity>> = projectRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun onDateSelected(date: Long) {
        _selectedDate.value = date
        // Clear input? Maybe keep it if user just changes date.
    }

    fun onProjectSelected(project: ProjectEntity) {
        _selectedProject.value = project
    }

    fun onHoursChanged(input: String) {
        _hours.value = input
    }

    fun onDescriptionChanged(input: String) {
        _description.value = input
    }

    fun onSaveClicked() {
        val project = _selectedProject.value ?: return
        val hoursVal = _hours.value.toDoubleOrNull()
        val desc = _description.value
        val date = _selectedDate.value
        
        if (isFutureDate(date)) {
            // Future -> Calendar Intent
            openCalendarEvent(project, date, desc)
        } else {
            // Past/Today -> Work Report
            if (hoursVal == null || hoursVal <= 0) return // Validate hours

            val saveAction = {
                viewModelScope.launch {
                    val report = com.antigravity.aegis.data.local.entity.WorkReportEntity(
                        projectId = project.id,
                        date = date,
                        description = desc.ifBlank { "Imputación Horaria" },
                        hours = hoursVal,
                        signaturePath = null // No signature for manual time entry usually
                    )
                    crmRepository.createWorkReport(report)
                    // Reset fields
                    _hours.value = ""
                    _description.value = ""
                    _selectedProject.value = null
                }
            }

            // Check Status
            if (project.status != ProjectStatus.ACTIVE) {
                _pendingSaveAction.value = { saveAction() } // Using invoke wrapper
                _showStatusDialog.value = true
            } else {
                saveAction()
            }
        }
    }

    fun onConfirmStatusChange() {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            projectRepository.updateProjectStatus(project.id, ProjectStatus.ACTIVE.name)
            _showStatusDialog.value = false
            _pendingSaveAction.value?.invoke()
            _pendingSaveAction.value = null
        }
    }

    fun onDismissStatusDialog() {
        _showStatusDialog.value = false
        _pendingSaveAction.value = null
    }

    private fun isFutureDate(date: Long): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        return date > today.timeInMillis + (24 * 60 * 60 * 1000) - 1 // Actually purely strictly greater than today end? 
        // Simplest: Check if date is after "now". But usually DatePicker gives 00:00. 
        // Let's say if selected date is AFTER today's date (ignoring time if selection is just date).
        // If DatePicker returns midnight of chosen day:
        // Future means > Today's Midnight + 24h? Or just > Today's Midnight?
        // Usually "Today" allows imputing. "Tomorrow" allows planning.
        return date > System.currentTimeMillis() // Rough check, user might pick today later hours.
        // Better: Compare Days.
    }
    
    // Improved Future Check comparing Day of Year
    fun isFutureDay(timestamp: Long): Boolean {
        val selected = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()
        
        if (selected.get(Calendar.YEAR) > now.get(Calendar.YEAR)) return true
        if (selected.get(Calendar.YEAR) < now.get(Calendar.YEAR)) return false
        return selected.get(Calendar.DAY_OF_YEAR) > now.get(Calendar.DAY_OF_YEAR)
    }

    private fun openCalendarEvent(project: ProjectEntity, date: Long, description: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Trabajo en: ${project.name}")
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date + (60 * 60 * 1000)) // Default 1 hour
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle no calendar app
        }
    }
}
