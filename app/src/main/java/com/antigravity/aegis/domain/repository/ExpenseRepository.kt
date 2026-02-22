package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun insertExpense(expense: ExpenseEntity): Result<Long>
    suspend fun updateExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun deleteExpense(expense: ExpenseEntity): Result<Unit>
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesByProject(projectId: Int): Flow<List<ExpenseEntity>>
    fun getExpensesWithoutProject(): Flow<List<ExpenseEntity>>
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    suspend fun getExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity>
    suspend fun getGeneralExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity>
}

