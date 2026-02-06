package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun insertExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesByProject(projectId: Int): Flow<List<ExpenseEntity>>
    fun getExpensesWithoutProject(): Flow<List<ExpenseEntity>>
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
}
