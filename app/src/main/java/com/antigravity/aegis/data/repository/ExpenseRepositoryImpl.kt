package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ExpenseDao
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.domain.repository.ExpenseRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override suspend fun insertExpense(expense: ExpenseEntity): Result<Long> = try {
        Result.Success(dao.insertExpense(expense))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateExpense(expense: ExpenseEntity): Result<Unit> = try {
        Result.Success(dao.updateExpense(expense))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity): Result<Unit> = try {
        Result.Success(dao.deleteExpense(expense))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    override fun getExpensesByProject(projectId: Int): Flow<List<ExpenseEntity>> = dao.getExpensesByProject(projectId)
    override fun getExpensesWithoutProject(): Flow<List<ExpenseEntity>> = dao.getExpensesWithoutProject()
    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> = dao.getExpensesByDateRange(startDate, endDate)
    override suspend fun getExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity> = dao.getExpensesByDateRangeSync(startDate, endDate)
    override suspend fun getGeneralExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity> = dao.getGeneralExpensesByDateRangeSync(startDate, endDate)
}

