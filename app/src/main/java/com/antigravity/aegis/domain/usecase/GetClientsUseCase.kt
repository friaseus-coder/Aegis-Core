package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClientsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(query: String = ""): Flow<List<Client>> {
        return if (query.isBlank()) {
            repository.getAllClients()
        } else {
            repository.searchClients(query)
        }
    }
}
