package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.util.getOrNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateQuoteFromProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(projectId: Int): Long {
        val project = projectRepository.getProjectById(projectId)
            ?: throw IllegalStateException("Project not found")

        val subProjects = projectRepository.getSubProjectsSync(projectId)

        val quote = QuoteEntity(
            clientId = project.clientId ?: 0,
            projectId = project.id,
            date = System.currentTimeMillis(),
            totalAmount = 0.0,
            status = "Draft",
            description = "Generado desde proyecto: ${project.name}",
            title = project.name,
            version = 1
        )

        val quoteId = budgetRepository.insertQuote(quote).getOrNull()
            ?: throw IllegalStateException("Error al crear el presupuesto")

        val lines = subProjects.map { subProject ->
            BudgetLineEntity(
                quoteId = quoteId.toInt(),
                description = subProject.name,
                quantity = 1.0,
                unitPrice = subProject.price ?: 0.0,
                taxRate = 0.21
            )
        }

        budgetRepository.insertBudgetLines(lines)

        return quoteId
    }
}

