package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.BudgetLogEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    // Quotes (Presupuestos)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): QuoteEntity?

    @Query("SELECT * FROM quotes ORDER BY date DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>
    
    @Query("SELECT * FROM quotes WHERE projectId = :projectId ORDER BY date DESC")
    fun getQuotesByProject(projectId: Int): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE projectId = :projectId ORDER BY date DESC LIMIT 1")
    suspend fun getQuoteByProjectId(projectId: Int): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE projectId = :projectId ORDER BY date DESC")
    suspend fun getQuotesByProjectSync(projectId: Int): List<QuoteEntity>

    @Query("UPDATE quotes SET status = :status WHERE id = :quoteId")
    suspend fun updateQuoteStatus(quoteId: Int, status: String)

    // Budget Lines
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetLine(line: BudgetLineEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetLines(lines: List<BudgetLineEntity>)

    @Query("SELECT * FROM budget_lines WHERE quoteId = :quoteId")
    fun getBudgetLines(quoteId: Int): Flow<List<BudgetLineEntity>>

    @Query("SELECT * FROM budget_lines WHERE quoteId = :quoteId")
    suspend fun getBudgetLinesSync(quoteId: Int): List<BudgetLineEntity>

    @Query("DELETE FROM budget_lines WHERE quoteId = :quoteId")
    suspend fun deleteBudgetLines(quoteId: Int)

    // Budget Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetLog(log: BudgetLogEntity): Long

    @Query("SELECT * FROM budget_logs WHERE quoteId = :quoteId ORDER BY timestamp DESC")
    fun getBudgetLogs(quoteId: Int): Flow<List<BudgetLogEntity>>

    @Transaction
    suspend fun saveBudgetTransaction(quote: QuoteEntity, lines: List<BudgetLineEntity>): Long {
        // 1. Insert or Update Quote
        val quoteId = if (quote.id == 0) {
            insertQuote(quote)
        } else {
            updateQuote(quote)
            quote.id.toLong()
        }

        // 2. Clear old lines if updating
        if (quote.id != 0) {
            deleteBudgetLines(quote.id)
        }

        // 3. Insert new lines
        val linesWithId = lines.map { it.copy(quoteId = quoteId.toInt()) }
        insertBudgetLines(linesWithId)

        return quoteId
    }
}
