package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.getOrNull
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SaveProjectAsTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: Int, templateName: String, category: String?) {
        // 1. Get original project
        val originalProject = projectRepository.getProjectById(projectId) ?: return

        // 2. Create Template Project
        val templateProject = ProjectEntity(
            clientId = originalProject.clientId,
            name = templateName,
            status = CrmStatus.ACTIVE,
            startDate = System.currentTimeMillis(),
            isTemplate = true,
            category = category
        )

        val templateId = projectRepository.insertProject(templateProject).getOrNull() ?: return

        // 3. Copy Tasks
        val tasks = taskRepository.getTasksByProject(projectId).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
            taskRepository.insertTask(
                task.copy(
                    id = 0,
                    projectId = templateId.toInt(),
                    isCompleted = false
                )
            )
        }

        // 4. Copy Subprojects (Recursive)
        val subProjects = projectRepository.getSubProjects(projectId).firstOrNull() ?: emptyList()
        subProjects.forEach { sub ->
            saveSubProjectAsTemplate(sub, templateId.toInt())
        }
    }

    private suspend fun saveSubProjectAsTemplate(subProject: ProjectEntity, parentTemplateId: Int) {
        val subTemplate = subProject.copy(
            id = 0,
            parentProjectId = parentTemplateId,
            status = CrmStatus.ACTIVE,
            isTemplate = true,
            startDate = System.currentTimeMillis(),
            endDate = null
        )
        val subTemplateId = projectRepository.insertProject(subTemplate).getOrNull() ?: return

        // Copy tasks for subproject
        val tasks = taskRepository.getTasksByProject(subProject.id).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
            taskRepository.insertTask(
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

