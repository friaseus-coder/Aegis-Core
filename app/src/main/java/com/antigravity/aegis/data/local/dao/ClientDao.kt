package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("SELECT * FROM clients ORDER BY firstName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getClientById(id: Int): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' ORDER BY firstName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    // Filtro por tipo de cliente
    @Query("SELECT * FROM clients WHERE tipoCliente = :tipoCliente ORDER BY firstName ASC")
    fun getClientsByType(tipoCliente: String): Flow<List<ClientEntity>>

    // Actualización de categoría
    @Query("UPDATE clients SET categoria = :categoria WHERE id = :clientId")
    suspend fun updateClientCategoria(clientId: Int, categoria: String)

    // Métodos sync para backup/restore
    @Query("SELECT * FROM clients")
    suspend fun getAllClientsSync(): List<ClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()
}
