package com.antigravity.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antigravity.aegis.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Query("SELECT * FROM documents WHERE clientId = :clientId ORDER BY dateAdded DESC")
    fun getDocumentsForClient(clientId: Int): Flow<List<DocumentEntity>>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): DocumentEntity?

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}
