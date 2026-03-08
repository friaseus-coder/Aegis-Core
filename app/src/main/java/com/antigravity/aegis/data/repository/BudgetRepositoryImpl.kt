package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.BudgetDao
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.BudgetLogEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.domain.repository.BudgetRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override suspend fun insertQuote(quote: QuoteEntity): Result<Long> = try {
        Result.Success(dao.insertQuote(quote))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateQuote(quote: QuoteEntity): Result<Unit> = try {
        Result.Success(dao.updateQuote(quote))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getQuoteById(id: Int): QuoteEntity? = dao.getQuoteById(id)
    override fun getAllQuotes(): Flow<List<QuoteEntity>> = dao.getAllQuotes()
    override fun getQuotesByProject(projectId: Int): Flow<List<QuoteEntity>> = dao.getQuotesByProject(projectId)
    override suspend fun getQuotesByProjectSuspend(projectId: Int): List<QuoteEntity> = dao.getQuotesByProjectSync(projectId)
    override suspend fun getQuoteByProjectId(projectId: Int): QuoteEntity? = dao.getQuoteByProjectId(projectId)

    override suspend fun updateQuoteStatus(quoteId: Int, status: String): Result<Unit> = try {
        Result.Success(dao.updateQuoteStatus(quoteId, status))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun insertBudgetLine(line: BudgetLineEntity): Result<Long> = try {
        Result.Success(dao.insertBudgetLine(line))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun insertBudgetLines(lines: List<BudgetLineEntity>): Result<Unit> = try {
        Result.Success(dao.insertBudgetLines(lines))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun getBudgetLines(quoteId: Int): Flow<List<BudgetLineEntity>> = dao.getBudgetLines(quoteId)
    override suspend fun getBudgetLinesSync(quoteId: Int): List<BudgetLineEntity> = dao.getBudgetLinesSync(quoteId)

    override suspend fun deleteBudgetLines(quoteId: Int): Result<Unit> = try {
        Result.Success(dao.deleteBudgetLines(quoteId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun insertBudgetLog(log: BudgetLogEntity): Result<Long> = try {
        Result.Success(dao.insertBudgetLog(log))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun getBudgetLogs(quoteId: Int): Flow<List<BudgetLogEntity>> = dao.getBudgetLogs(quoteId)

    override suspend fun saveQuoteWithLines(quote: QuoteEntity, lines: List<BudgetLineEntity>): Result<Long> = try {
        Result.Success(dao.saveBudgetTransaction(quote, lines))
    } catch (e: Exception) {
        Result.Error(e)
    }
}

