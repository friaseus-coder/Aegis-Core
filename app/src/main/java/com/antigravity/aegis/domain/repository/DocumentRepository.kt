package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.DocumentEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de repositorio para operaciones con Documentos de Clientes.
 * Extraído del monolito CrmRepository para cumplir con SRP.
 */
interface DocumentRepository {
    fun getDocumentsForClient(clientId: Int): Flow<List<DocumentEntity>>
    suspend fun addDocument(document: DocumentEntity): Result<Long>
    suspend fun deleteDocument(document: DocumentEntity): Result<Unit>
    suspend fun getAllDocumentsSync(): List<DocumentEntity>
}
