package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.SessionDao
import com.antigravity.aegis.data.local.entity.SessionEntity
import com.antigravity.aegis.domain.repository.SessionRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun insertSession(session: SessionEntity): Result<Long> {
        return try {
            val id = sessionDao.insertSession(session)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateSession(session: SessionEntity): Result<Unit> {
        return try {
            sessionDao.updateSession(session)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteSession(session: SessionEntity): Result<Unit> {
        return try {
            sessionDao.deleteSession(session)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getSessionsByProject(projectId: Int): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsByProject(projectId)
    }

    override suspend fun getSessionById(id: Int): SessionEntity? {
        return sessionDao.getSessionById(id)
    }

    override fun getSessionsByClient(clientId: Int): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsByClient(clientId)
    }
}
