package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.domain.model.Client
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getAllClients(): Flow<List<Client>>
    fun getClientById(id: Int): Flow<Client?>
    suspend fun insertClient(client: Client): Int
    suspend fun updateClient(client: Client)
    suspend fun deleteClient(client: Client)
    fun searchClients(query: String): Flow<List<Client>>
    
    // Filtro por tipo de cliente
    fun getClientsByType(tipoCliente: String): Flow<List<Client>>
    
    // Actualización de categoría
    suspend fun updateClientCategoria(clientId: Int, categoria: String)
    
    // Método sync para backup
    suspend fun getAllClientsSync(): List<Client>
}
