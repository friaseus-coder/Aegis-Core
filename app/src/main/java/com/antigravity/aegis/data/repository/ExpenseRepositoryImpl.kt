package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ExpenseDao
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {
    override suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)
    override suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    override fun getExpensesByProject(projectId: Int): Flow<List<ExpenseEntity>> = dao.getExpensesByProject(projectId)
    override fun getExpensesWithoutProject(): Flow<List<ExpenseEntity>> = dao.getExpensesWithoutProject()
    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> = dao.getExpensesByDateRange(startDate, endDate)
}
