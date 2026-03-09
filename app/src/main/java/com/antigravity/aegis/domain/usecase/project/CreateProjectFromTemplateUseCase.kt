package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.getOrNull
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CreateProjectFromTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(
        templateId: Int,
        newClientId: Int,
        newProjectName: String,
        startDate: Long,
        deadline: Long?
    ): Long {
        // 1. Verify Template
        val template = projectRepository.getProjectById(templateId)
            ?: throw IllegalArgumentException("Template not found")
        if (!template.isTemplate) throw IllegalArgumentException("Selected project is not a template")

        // 2. Create Root Project
        val newProject = ProjectEntity(
            clientId = newClientId,
            name = newProjectName,
            status = CrmStatus.DRAFT,
            startDate = startDate,
            endDate = deadline,
            isTemplate = false
        )
        val newProjectId = projectRepository.insertProject(newProject).getOrNull()
            ?: throw IllegalStateException("Error al crear el proyecto desde la plantilla")

        // 3. Copy Content (Tasks and Subprojects)
        val copiedSubProjects = copyContent(templateId, newProjectId.toInt())
        
        // 4. Create Draft Quote for new project
        createDraftQuoteForTemplate(newProjectId.toInt(), newClientId, newProjectName, copiedSubProjects)
        
        return newProjectId
    }

    private suspend fun createDraftQuoteForTemplate(projectId: Int, clientId: Int, projectName: String, subProjects: List<ProjectEntity>) {
        val estimatedTotal = subProjects.sumOf { it.price ?: 0.0 }
        val totalWithTax = estimatedTotal * 1.21

        val quote = com.antigravity.aegis.data.local.entity.QuoteEntity(
            clientId = clientId,
            projectId = projectId,
            date = System.currentTimeMillis(),
            totalAmount = totalWithTax,
            status = CrmStatus.DRAFT,
            description = "Presupuesto desde plantilla: $projectName",
            title = projectName,
            calculatedTotal = (totalWithTax * 100).toLong(),
            version = 1
        )
        val quoteId = budgetRepository.insertQuote(quote).getOrNull() ?: return

        if (subProjects.isNotEmpty()) {
            val lines = subProjects.map { sub ->
                com.antigravity.aegis.data.local.entity.BudgetLineEntity(
                    quoteId = quoteId.toInt(),
                    description = buildString {
                        append(sub.name)
                        if (!sub.materials.isNullOrBlank()) append(" | Mat: ${sub.materials}")
                        if (sub.estimatedTime != null && sub.estimatedTimeUnit != null) {
                            append(" | ${sub.estimatedTime} ${sub.estimatedTimeUnit}")
                        }
                    },
                    quantity = 1.0,
                    unitPrice = sub.price ?: 0.0,
                    taxRate = 0.21
                )
            }
            budgetRepository.insertBudgetLines(lines)
        }
    }

    private suspend fun copyContent(sourceProjectId: Int, targetProjectId: Int): List<ProjectEntity> {
        val copiedSubProjects = mutableListOf<ProjectEntity>()

        // Copy Tasks
        val tasks = taskRepository.getTasksByProject(sourceProjectId).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
            taskRepository.insertTask(
                task.copy(
                    id = 0,
                    projectId = targetProjectId,
                    isCompleted = false
                )
            )
        }

        // Copy Subprojects
        val subProjects = projectRepository.getSubProjects(sourceProjectId).firstOrNull() ?: emptyList()
        subProjects.forEach { sub ->
            val parent = projectRepository.getProjectById(targetProjectId)
            val correctClientId = parent?.clientId ?: sub.clientId

            val finalSub = sub.copy(
                id = 0,
                parentProjectId = targetProjectId,
                clientId = correctClientId,
                status = CrmStatus.ACTIVE,
                startDate = parent?.startDate ?: System.currentTimeMillis(),
                endDate = parent?.endDate,
                isTemplate = false
            )

            val newSubId = projectRepository.insertProject(finalSub).getOrNull() ?: return@forEach
            copiedSubProjects.add(finalSub)

            // Recursive copy
            val nested = copyContent(sub.id, newSubId.toInt())
            copiedSubProjects.addAll(nested)
        }

        return copiedSubProjects
    }
}

