package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.BudgetLogEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // Quotes / Presupuestos
    suspend fun insertQuote(quote: QuoteEntity): Long
    suspend fun updateQuote(quote: QuoteEntity)
    suspend fun getQuoteById(id: Int): QuoteEntity?
    fun getAllQuotes(): Flow<List<QuoteEntity>>
    fun getQuotesByProject(projectId: Int): Flow<List<QuoteEntity>>
    suspend fun getQuotesByProjectSuspend(projectId: Int): List<QuoteEntity>
    suspend fun updateQuoteStatus(quoteId: Int, status: String)

    // Lines
    suspend fun insertBudgetLine(line: BudgetLineEntity): Long
    suspend fun insertBudgetLines(lines: List<BudgetLineEntity>)
    fun getBudgetLines(quoteId: Int): Flow<List<BudgetLineEntity>>
    suspend fun deleteBudgetLines(quoteId: Int)

    // Logs
    suspend fun insertBudgetLog(log: BudgetLogEntity): Long
    fun getBudgetLogs(quoteId: Int): Flow<List<BudgetLogEntity>>
    
    suspend fun saveQuoteWithLines(quote: QuoteEntity, lines: List<BudgetLineEntity>): Long
}
