package com.antigravity.aegis.domain.usecase.crm

import android.content.Context
import com.antigravity.aegis.domain.reports.PdfGenerator
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ClientRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.SettingsRepository
import com.antigravity.aegis.domain.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import javax.inject.Inject

/**
 * Genera un PDF de un presupuesto (Quote) dado su ID.
 *
 * El PDF incluye:
 * - Datos del proveedor (empresa) a la izquierda
 * - Datos del cliente a la derecha
 * - Título con número de presupuesto y fecha
 * - Tabla con el proyecto y sus subproyectos como conceptos a cobrar
 * - Totales (base imponible, IVA, total)
 *
 * @return Result<File> con el archivo PDF generado
 */
class GenerateQuotePdfUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val projectRepository: ProjectRepository,
    private val clientRepository: ClientRepository,
    private val settingsRepository: SettingsRepository,
    private val pdfGenerator: PdfGenerator,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(quoteId: Int): Result<File> {
        return try {
            // 1. Recuperar el quote
            val quote = budgetRepository.getQuoteById(quoteId)
                ?: return Result.Error(IllegalStateException("Presupuesto #$quoteId no encontrado"))

            // 2. Recuperar el cliente
            val client = clientRepository.getClientById(quote.clientId).firstOrNull()
                ?: return Result.Error(IllegalStateException("Cliente no encontrado para el presupuesto #$quoteId"))

            // 3. Recuperar las líneas del presupuesto (budget lines)
            val lines = budgetRepository.getBudgetLinesSync(quoteId)

            // 4. (Eliminado: ya no pasamos subproyectos al PDF, preferimos las líneas de presupuesto reales)

            // 5. Recuperar configuración de empresa
            val config = settingsRepository.getUserConfig().firstOrNull()

            // 6. Generar el PDF con el layout mejorado
            val pdfFile = pdfGenerator.generateQuotePdf(
                context = context,
                quote = quote,
                client = client,
                config = config,
                budgetLines = lines
            )

            Result.Success(pdfFile)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
