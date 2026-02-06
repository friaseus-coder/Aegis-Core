package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.BudgetDao
import com.antigravity.aegis.data.model.BudgetLineEntity
import com.antigravity.aegis.data.model.BudgetLogEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {
    override suspend fun insertQuote(quote: QuoteEntity): Long = dao.insertQuote(quote)
    override suspend fun updateQuote(quote: QuoteEntity) = dao.updateQuote(quote)
    override suspend fun getQuoteById(id: Int): QuoteEntity? = dao.getQuoteById(id)
    override fun getAllQuotes(): Flow<List<QuoteEntity>> = dao.getAllQuotes()
    override fun getQuotesByProject(projectId: Int): Flow<List<QuoteEntity>> = dao.getQuotesByProject(projectId)
    override suspend fun getQuotesByProjectSuspend(projectId: Int): List<QuoteEntity> = dao.getQuotesByProjectSync(projectId)
    override suspend fun updateQuoteStatus(quoteId: Int, status: String) = dao.updateQuoteStatus(quoteId, status)
    
    override suspend fun insertBudgetLine(line: BudgetLineEntity): Long = dao.insertBudgetLine(line)
    override suspend fun insertBudgetLines(lines: List<BudgetLineEntity>) = dao.insertBudgetLines(lines)
    override fun getBudgetLines(quoteId: Int): Flow<List<BudgetLineEntity>> = dao.getBudgetLines(quoteId)
    override suspend fun deleteBudgetLines(quoteId: Int) = dao.deleteBudgetLines(quoteId)
    
    override suspend fun insertBudgetLog(log: BudgetLogEntity): Long = dao.insertBudgetLog(log)
    override fun getBudgetLogs(quoteId: Int): Flow<List<BudgetLogEntity>> = dao.getBudgetLogs(quoteId)
}
