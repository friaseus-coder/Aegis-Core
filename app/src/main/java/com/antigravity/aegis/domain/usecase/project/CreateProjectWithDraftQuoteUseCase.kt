package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.util.Result
import com.antigravity.aegis.domain.util.getOrNull
import javax.inject.Inject

/**
 * Datos de un subproyecto para crear junto al proyecto principal.
 */
data class SubProjectInput(
    val name: String,
    val materials: String? = null,
    val price: Double? = null,
    val estimatedTime: Double? = null,
    val estimatedTimeUnit: String? = null
)

/**
 * Crea un proyecto padre con sus subproyectos y genera automáticamente
 * un Presupuesto/Quote en estado DRAFT vinculado al proyecto.
 *
 * @return Result<Long> con el ID del proyecto creado
 */
class CreateProjectWithDraftQuoteUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        clientId: Int?,
        category: String?,
        startDate: Long,
        endDate: Long?,
        subProjects: List<SubProjectInput> = emptyList()
    ): Result<Long> {
        return try {
            // 1. Insertar el proyecto padre
            val parentProject = ProjectEntity(
                clientId = clientId,
                name = name,
                description = description,
                category = category,
                status = CrmStatus.ACTIVE,
                startDate = startDate,
                endDate = endDate
            )
            val projectId = projectRepository.insertProject(parentProject).getOrNull()
                ?: return Result.Error(IllegalStateException("Error al insertar el proyecto"))

            // 2. Insertar cada subproyecto
            for (sub in subProjects) {
                val subEntity = ProjectEntity(
                    clientId = clientId,
                    parentProjectId = projectId.toInt(),
                    name = sub.name,
                    status = CrmStatus.ACTIVE,
                    startDate = startDate,
                    materials = sub.materials,
                    price = sub.price,
                    estimatedTime = sub.estimatedTime,
                    estimatedTimeUnit = sub.estimatedTimeUnit
                )
                projectRepository.insertProject(subEntity)
            }

            // 3. Calcular el total estimado sumando precios de subproyectos
            val estimatedTotal = subProjects.sumOf { it.price ?: 0.0 }
            val totalWithTax = estimatedTotal * 1.21

            // 4. Crear el Quote en estado DRAFT vinculado al proyecto
            val quote = QuoteEntity(
                clientId = clientId ?: 0,
                projectId = projectId.toInt(),
                date = System.currentTimeMillis(),
                totalAmount = totalWithTax,
                status = CrmStatus.DRAFT,
                description = description ?: "Presupuesto generado desde proyecto: $name",
                title = name,
                calculatedTotal = (totalWithTax * 100).toLong(),
                version = 1
            )
            val quoteId = budgetRepository.insertQuote(quote).getOrNull()
                ?: return Result.Error(IllegalStateException("Error al crear el presupuesto DRAFT"))

            // 5. Crear las líneas del presupuesto con los subproyectos
            if (subProjects.isNotEmpty()) {
                val lines = subProjects.mapIndexed { index, sub ->
                    BudgetLineEntity(
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

            Result.Success(projectId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
