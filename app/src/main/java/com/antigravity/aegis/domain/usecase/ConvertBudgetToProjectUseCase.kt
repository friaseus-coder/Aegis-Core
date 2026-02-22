package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.getOrNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConvertBudgetToProjectUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(quoteId: Int) {
        val quote = budgetRepository.getQuoteById(quoteId) ?: return

        // 1. Mark as WON
        budgetRepository.updateQuoteStatus(quoteId, CrmStatus.WON)

        // 2. Create Project
        val newProject = ProjectEntity(
            clientId = quote.clientId,
            name = quote.title,
            status = CrmStatus.ACTIVE,
            startDate = System.currentTimeMillis()
        )

        val projectId = projectRepository.insertProject(newProject).getOrNull()?.toInt() ?: return

        // Update Quote with new ProjectID
        budgetRepository.updateQuote(quote.copy(projectId = projectId))

        // 3. Generate Tasks from BudgetLines
        val lines = budgetRepository.getBudgetLines(quoteId).first()
        lines.forEach { line ->
            val newTask = TaskEntity(
                projectId = projectId,
                title = line.description,
                description = "Generated from budget line. Qty: ${line.quantity}",
                isCompleted = false,
                isActive = true,
                status = "Pending"
            )
            taskRepository.insertTask(newTask)

            // 4. Descontar stock
            // TODO: Cannot deduct stock because BudgetLineEntity does not link to ProductEntity.
        }
    }
}

