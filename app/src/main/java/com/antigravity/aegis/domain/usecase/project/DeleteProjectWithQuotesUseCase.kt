package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

/**
 * Elimina un proyecto y toda su información asociada en el CRM:
 * - Quotes (presupuestos) vinculadas al proyecto
 * - Budget Lines de cada quote
 * - Subproyectos hijos
 * - El proyecto padre
 */
class DeleteProjectWithQuotesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(projectId: Int): Result<Unit> {
        return try {
            // 1. Borrar las budget lines y quotes del proyecto principal
            val quotes = budgetRepository.getQuotesByProjectSuspend(projectId)
            for (quote in quotes) {
                budgetRepository.deleteBudgetLines(quote.id)
                budgetRepository.deleteQuote(quote.id)
            }

            // 2. Borrar subproyectos (los hijos)
            val subProjects = projectRepository.getSubProjectsSync(projectId)
            for (sub in subProjects) {
                // Borrar quotes de cada subproyecto también
                val subQuotes = budgetRepository.getQuotesByProjectSuspend(sub.id)
                for (subQuote in subQuotes) {
                    budgetRepository.deleteBudgetLines(subQuote.id)
                    budgetRepository.deleteQuote(subQuote.id)
                }
                projectRepository.deleteProject(sub)
            }

            // 3. Borrar el proyecto padre
            val project = projectRepository.getProjectById(projectId)
                ?: return Result.Error(IllegalStateException("Proyecto con id=$projectId no encontrado"))
            projectRepository.deleteProject(project)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
