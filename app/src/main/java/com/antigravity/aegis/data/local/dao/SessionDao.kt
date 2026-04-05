package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE projectId = :projectId ORDER BY date DESC")
    fun getSessionsByProject(projectId: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): SessionEntity?

    @Query("SELECT * FROM sessions WHERE projectId IN (SELECT id FROM projects WHERE clientId = :clientId) ORDER BY date DESC")
    fun getSessionsByClient(clientId: Int): Flow<List<SessionEntity>>
}
