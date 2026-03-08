package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.BudgetLogEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // Quotes / Presupuestos
    suspend fun insertQuote(quote: QuoteEntity): Result<Long>
    suspend fun updateQuote(quote: QuoteEntity): Result<Unit>
    suspend fun getQuoteById(id: Int): QuoteEntity?
    fun getAllQuotes(): Flow<List<QuoteEntity>>
    fun getQuotesByProject(projectId: Int): Flow<List<QuoteEntity>>
    suspend fun getQuotesByProjectSuspend(projectId: Int): List<QuoteEntity>
    suspend fun getQuoteByProjectId(projectId: Int): QuoteEntity?
    suspend fun updateQuoteStatus(quoteId: Int, status: String): Result<Unit>

    // Lines
    suspend fun insertBudgetLine(line: BudgetLineEntity): Result<Long>
    suspend fun insertBudgetLines(lines: List<BudgetLineEntity>): Result<Unit>
    fun getBudgetLines(quoteId: Int): Flow<List<BudgetLineEntity>>
    suspend fun getBudgetLinesSync(quoteId: Int): List<BudgetLineEntity>
    suspend fun deleteBudgetLines(quoteId: Int): Result<Unit>

    // Logs
    suspend fun insertBudgetLog(log: BudgetLogEntity): Result<Long>
    fun getBudgetLogs(quoteId: Int): Flow<List<BudgetLogEntity>>

    suspend fun saveQuoteWithLines(quote: QuoteEntity, lines: List<BudgetLineEntity>): Result<Long>
}
