package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.ProjectStatus
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CreateProjectFromTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository,
    private val budgetRepository: com.antigravity.aegis.domain.repository.BudgetRepository
) {
    suspend operator fun invoke(
        templateId: Int,
        newClientId: Int,
        newProjectName: String,
        startDate: Long,
        deadline: Long?
    ): Long {
        // 1. Verify Template
        val template = projectRepository.getProjectById(templateId) ?: throw IllegalArgumentException("Template not found")
        if (!template.isTemplate) throw IllegalArgumentException("Selected project is not a template")

        // 2. Create Root Project
        val newProject = ProjectEntity(
            clientId = newClientId,
            name = newProjectName,
            status = ProjectStatus.ACTIVE,
            startDate = startDate,
            endDate = deadline,
            isTemplate = false // It's a real project
        )
        val newProjectId = projectRepository.insertProject(newProject)

        // 3. Copy Content (Tasks and Subprojects)
        copyContent(templateId, newProjectId.toInt())
        copyBudgets(templateId, newProjectId.toInt())
        
        return newProjectId
    }

    private suspend fun copyBudgets(sourceProjectId: Int, targetProjectId: Int) {
        val quotes = budgetRepository.getQuotesByProjectSuspend(sourceProjectId)
        val targetProject = projectRepository.getProjectById(targetProjectId)
        
        quotes.forEach { quote ->
            val newQuote = quote.copy(
                id = 0,
                projectId = targetProjectId,
                clientId = targetProject?.clientId ?: 0,
                status = "Draft", 
                date = System.currentTimeMillis()
            )
            val newQuoteId = budgetRepository.insertQuote(newQuote)
            
            // Get lines from Flow
            val lines = budgetRepository.getBudgetLines(quote.id).firstOrNull() ?: emptyList()
            lines.forEach { line ->
                 budgetRepository.insertBudgetLine(
                     line.copy(
                         id = 0,
                         quoteId = newQuoteId.toInt()
                     )
                 )
            }
        }
    }

    private suspend fun copyContent(sourceProjectId: Int, targetProjectId: Int) {
        // Copy Tasks
        val tasks = crmRepository.getTasksForProject(sourceProjectId).firstOrNull() ?: emptyList()
        tasks.forEach { task ->
            crmRepository.createTask(
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
            val newSubProject = sub.copy(
                id = 0,
                parentProjectId = targetProjectId,
                clientId = sub.clientId
            )
            
             val parent = projectRepository.getProjectById(targetProjectId)
             val correctClientId = parent?.clientId ?: sub.clientId
             
             val finalSub = newSubProject.copy(
                 clientId = correctClientId,
                 status = ProjectStatus.ACTIVE,
                 startDate = parent?.startDate ?: System.currentTimeMillis(),
                 endDate = parent?.endDate,
                 isTemplate = false
             )

            val newSubId = projectRepository.insertProject(finalSub)
            
            // Recursive copy
            copyContent(sub.id, newSubId.toInt())
            copyBudgets(sub.id, newSubId.toInt())
        }
    }
}
