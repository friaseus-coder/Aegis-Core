package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ClientDao
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.domain.model.Address
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientCategory
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val clientDao: ClientDao
) : ClientRepository {

    override fun getAllClients(): Flow<List<Client>> {
        return clientDao.getAllClients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getClientById(id: Int): Flow<Client?> {
        return clientDao.getClientById(id).map { it?.toDomain() }
    }

    override suspend fun insertClient(client: Client): Int {
        return clientDao.insertClient(client.toEntity()).toInt()
    }

    override suspend fun updateClient(client: Client) {
        clientDao.updateClient(client.toEntity())
    }

    override suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client.toEntity())
    }

    override fun searchClients(query: String): Flow<List<Client>> {
        return clientDao.searchClients(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getClientsByType(tipoCliente: String): Flow<List<Client>> {
        return clientDao.getClientsByType(tipoCliente).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateClientCategoria(clientId: Int, categoria: String) {
        clientDao.updateClientCategoria(clientId, categoria)
    }

    override suspend fun getAllClientsSync(): List<Client> {
        return clientDao.getAllClientsSync().map { it.toDomain() }
    }

    // Mappers
    private fun ClientEntity.toDomain(): Client {
        return Client(
            id = id,
            firstName = firstName,
            lastName = lastName,
            tipoCliente = try { ClientType.valueOf(tipoCliente.uppercase()) } catch (e: Exception) { ClientType.PARTICULAR },
            razonSocial = razonSocial,
            nifCif = nifCif,
            personaContacto = personaContacto,
            phone = phone,
            email = email,
            address = Address(calle, numero, piso, poblacion, codigoPostal),
            categoria = try { ClientCategory.valueOf(categoria.uppercase()) } catch (e: Exception) { ClientCategory.POTENTIAL },
            notas = notas
        )
    }

    private fun Client.toEntity(): ClientEntity {
        return ClientEntity(
            id = id,
            firstName = firstName,
            lastName = lastName,
            tipoCliente = tipoCliente.name, // o name.lowercase().capitalize() si se prefiere formato visual
            razonSocial = razonSocial,
            nifCif = nifCif,
            personaContacto = personaContacto,
            phone = phone,
            email = email,
            calle = address?.calle,
            numero = address?.numero,
            piso = address?.piso,
            poblacion = address?.poblacion,
            codigoPostal = address?.codigoPostal,
            categoria = categoria.name,
            notas = notas
        )
    }
}
