package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoseBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(quoteId: Int) {
        // 1. Mark Quote as LOST
        budgetRepository.updateQuoteStatus(quoteId, "Lost")

        val quote = budgetRepository.getQuoteById(quoteId) ?: return

        // 2. Change Project Status to ARCHIVED (if linked)
        quote.projectId?.let { projectId ->
            // Update Project Status
            projectRepository.updateProjectStatus(projectId, CrmStatus.ARCHIVED)

            // 3. Mark Tasks as Cancelled
            val tasks = taskRepository.getTasksByProject(projectId).first()
            tasks.forEach { task ->
                val updatedTask = task.copy(
                    isActive = false,
                    status = "Cancelled"
                )
                taskRepository.updateTask(updatedTask)
            }

            // 4. Liberar reservas de stock
            // TODO: Cannot release stock because BudgetLineEntity does not link to ProductEntity.
        }
    }
}

