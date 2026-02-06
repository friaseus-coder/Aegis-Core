package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.ProjectStatus
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConvertBudgetToProjectUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository
) {
    suspend operator fun invoke(quoteId: Int) {
        val quote = budgetRepository.getQuoteById(quoteId) ?: return

        // 1. Mark as WON (Accepted)
        budgetRepository.updateQuoteStatus(quoteId, "Accepted")

        // 2. Create Project
        // Determine status, startDate etc.
        val newProject = ProjectEntity(
            clientId = quote.clientId,
            name = quote.title, // Use title as project name
            status = ProjectStatus.ACTIVE,
            startDate = System.currentTimeMillis()
        )
        
        val projectId = projectRepository.insertProject(newProject).toInt()
        
        // Update Quote with new ProjectID
        budgetRepository.updateQuote(quote.copy(projectId = projectId))

        // 3. Generate Tasks from BudgetLines
        val lines = budgetRepository.getBudgetLines(quoteId).first()
        lines.forEach { line ->
            // Create a Task from each line
            val newTask = TaskEntity(
                projectId = projectId,
                title = line.description,
                description = "Generated from budget line. Qty: ${line.quantity}",
                isCompleted = false,
                isActive = true,
                status = "Pending"
            )
            crmRepository.createTask(newTask)
            
            // 4. Descontar stock
            // TODO: Cannot deduct stock because BudgetLineEntity does not link to ProductEntity.
        }
    }
}
