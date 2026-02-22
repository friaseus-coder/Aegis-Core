package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.DocumentDao
import com.antigravity.aegis.data.local.entity.DocumentEntity
import com.antigravity.aegis.domain.repository.DocumentRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de Documentos.
 */
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getDocumentsForClient(clientId: Int): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsForClient(clientId)

    override suspend fun addDocument(document: DocumentEntity): Result<Long> = try {
        Result.Success(documentDao.insertDocument(document))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteDocument(document: DocumentEntity): Result<Unit> = try {
        documentDao.deleteDocument(document)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAllDocumentsSync(): List<DocumentEntity> =
        documentDao.getAllDocumentsSync()
}

