package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.SessionEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun insertSession(session: SessionEntity): Result<Long>
    suspend fun updateSession(session: SessionEntity): Result<Unit>
    suspend fun deleteSession(session: SessionEntity): Result<Unit>
    fun getSessionsByProject(projectId: Int): Flow<List<SessionEntity>>
    suspend fun getSessionById(id: Int): SessionEntity?
    fun getSessionsByClient(clientId: Int): Flow<List<SessionEntity>>
}
