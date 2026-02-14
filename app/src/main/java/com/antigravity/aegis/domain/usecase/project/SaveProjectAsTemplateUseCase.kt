package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.ProjectStatus
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SaveProjectAsTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository // For tasks
) {
    suspend operator fun invoke(projectId: Int, templateName: String, category: String?) {
        // 1. Get original project
        val originalProject = projectRepository.getProjectById(projectId) ?: return
        
        // 2. Create Template Project
        val templateProject = ProjectEntity(
            clientId = originalProject.clientId, // Keep client? Or set specific "Template Client"? 
            // Templates might be client-agnostic or specific. 
            // For now, keep generic or require a dummy client. 
            // Usually templates are not linked to a client. 
            // But DB schema requires clientId NOT NULL.
            // We can reuse the same client or a system client.
            // Let's keep the original clientId for simplicity or maybe we should change schema to nullable?
            // Changing schema schema to nullable clientId might be complex.
            // Let's copy it for now.
            name = templateName,
            status = ProjectStatus.ACTIVE, // Templates are always "Active" in terms of visibility in template list
            startDate = System.currentTimeMillis(),
            isTemplate = true,
            category = category
        )
        
        val templateId = projectRepository.insertProject(templateProject)
        
        // 3. Copy Tasks
        val tasks = crmRepository.getTasksForProject(projectId).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
            crmRepository.createTask(
                task.copy(
                    id = 0,
                    projectId = templateId.toInt(),
                    isCompleted = false // Reset status
                )
            )
        }
        
        // 4. Copy Subprojects (Recursive?)
        // If we want deep copy.
        val subProjects = projectRepository.getSubProjects(projectId).firstOrNull() ?: emptyList()
        subProjects.forEach { sub ->
            // Recursive call logic or just manual copy
            saveSubProjectAsTemplate(sub, templateId.toInt())
        }
    }
    
    private suspend fun saveSubProjectAsTemplate(subProject: ProjectEntity, parentTemplateId: Int) {
        val subTemplate = subProject.copy(
            id = 0,
            parentProjectId = parentTemplateId,
            status = ProjectStatus.ACTIVE,
            isTemplate = true,
            startDate = System.currentTimeMillis(),
            endDate = null
        )
        val subTemplateId = projectRepository.insertProject(subTemplate)
        
        // Copy tasks for subproject
        val tasks = crmRepository.getTasksForProject(subProject.id).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
             crmRepository.createTask(
                task.copy(
                    id = 0,
                    projectId = subTemplateId.toInt(),
                    isCompleted = false
                )
            )
        }
        
        // Recursion for sub-subprojects if needed
        val subSubProjects = projectRepository.getSubProjects(subProject.id).firstOrNull() ?: emptyList()
        subSubProjects.forEach { subSub ->
            saveSubProjectAsTemplate(subSub, subTemplateId.toInt())
        }
    }
}
