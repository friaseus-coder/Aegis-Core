package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY date DESC")
    fun getExpensesByProject(projectId: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY date DESC")
    suspend fun getExpensesByProjectSync(projectId: Int): List<ExpenseEntity>
    
    @Query("SELECT * FROM expenses WHERE projectId IS NULL ORDER BY date DESC")
    fun getExpensesWithoutProject(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE projectId IS NULL AND date BETWEEN :startDate AND :endDate")
    suspend fun getGeneralExpensesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntity>
}
