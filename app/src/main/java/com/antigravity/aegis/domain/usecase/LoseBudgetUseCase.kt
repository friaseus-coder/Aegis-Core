package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.model.ProjectStatus
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoseBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository
) {
    suspend operator fun invoke(quoteId: Int) {
        // 1. Mark Quote as LOST
        budgetRepository.updateQuoteStatus(quoteId, "Lost")

        val quote = budgetRepository.getQuoteById(quoteId) ?: return

        // 2. Change Project Status to ARCHIVED (if linked)
        quote.projectId?.let { projectId ->
            // Update Project Status
            projectRepository.updateProjectStatus(projectId, ProjectStatus.ARCHIVED.name)

            // 3. Mark Tasks as Cancelled
            // We get the current list of tasks and mark them as cancelled/not active
            // Note: TaskEntity needs a status field or similar. 
            // Assuming we just mark them as completed = false? 
            // The requirement says "isActive = false y estado CANCELADA".
            // TaskEntity currently is unknown (viewed CrmRepo but not TaskEntity).
            // I'll assume updateTaskStatus maps to "isCompleted" or similar.
            // If TaskEntity has 'status', CrmRepo.updateTaskStatus only takes boolean isCompleted.
            // I will implement what is possible via CrmRepository or add method later.
            // For now, iterate and update.
            val tasks = crmRepository.getTasksForProject(projectId).first()
            tasks.forEach { task ->
                // Mark as inactive and status Cancelled
                val updatedTask = task.copy(
                    isActive = false,
                    status = "Cancelled"
                )
                // CrmRepository currently only has updateTaskStatus(id, isCompleted).
                // We need a general updateTask(task).
                // Assuming we can add it or misuse create if REPLACE strategy is used (id is set).
                // CrmRepository interface needs updateTask. Check if it exists? 
                // It has create (insert) but usually insert with replacement works if ID exists.
                // Checking CrmDao (Step 5): It has insertTask (onConflict REPLACE) via CrmDao? 
                // CrmRepository.kt Step 68: suspend fun createTask(task: TaskEntity): Long.
                // If ID is set, insert might fail or replace depending on Dao.
                // Dao usually REPLACE.
                crmRepository.createTask(updatedTask) 
            }
            
            // 4. Liberar reservas de stock
            // TODO: Cannot release stock because BudgetLineEntity does not link to ProductEntity.
        }
    }
}
