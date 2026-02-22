package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.repository.ExpenseRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

data class ProjectProfitability(
    val projectId: Int,
    val totalIncome: Double,
    val directExpenses: Double,
    val allocatedGeneralExpenses: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMargin: Double
)

class GetProjectRealProfitUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(projectId: Int): ProjectProfitability? {
        val project = projectRepository.getProjectById(projectId) ?: return null

        // 1. Calculate Income (Accepted Quotes)
        val quotes = budgetRepository.getQuotesByProjectSuspend(projectId)
        val totalIncome = quotes.filter { it.status == "Accepted" }.sumOf { it.totalAmount }

        // 2. Direct Expenses for this project
        val directExpensesList = expenseRepository.getExpensesByProject(projectId).first()
        val directExpensesTotal = directExpensesList.sumOf { it.totalAmount }

        // 3. Prorate general expenses (without project) across active projects per month
        var totalGeneralExpenses = 0.0

        val startCal = Calendar.getInstance().apply { timeInMillis = project.startDate }
        val endCal = Calendar.getInstance().apply { timeInMillis = project.endDate ?: System.currentTimeMillis() }

        val iteratorCal = startCal.clone() as Calendar
        iteratorCal.set(Calendar.DAY_OF_MONTH, 1)

        while (iteratorCal.timeInMillis <= endCal.timeInMillis) {
            val monthStart = iteratorCal.timeInMillis
            val nextMonth = iteratorCal.clone() as Calendar
            nextMonth.add(Calendar.MONTH, 1)
            val monthEnd = nextMonth.timeInMillis - 1

            val generalExpenses = expenseRepository.getGeneralExpensesByDateRangeSync(monthStart, monthEnd)
            val monthlyGeneralTotal = generalExpenses.sumOf { it.totalAmount }

            if (monthlyGeneralTotal > 0) {
                val activeProjects = projectRepository.getProjectsActiveInPeriod(monthStart, monthEnd)
                val count = activeProjects.size
                if (count > 0) totalGeneralExpenses += (monthlyGeneralTotal / count)
            }

            iteratorCal.add(Calendar.MONTH, 1)
        }

        val netProfit = totalIncome - directExpensesTotal - totalGeneralExpenses
        val totalExpenses = directExpensesTotal + totalGeneralExpenses
        val profitMargin = if (totalIncome > 0) (netProfit / totalIncome) * 100 else 0.0

        return ProjectProfitability(
            projectId, totalIncome, directExpensesTotal, totalGeneralExpenses, totalExpenses, netProfit, profitMargin
        )
    }
}
