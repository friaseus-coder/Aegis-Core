package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.repository.ClientRepository
import com.antigravity.aegis.domain.util.Result
import javax.inject.Inject

class AddClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(client: Client): Result<Int> {
        return try {
            if (client.firstName.isBlank()) {
                return Result.Error(IllegalArgumentException("Name cannot be empty"))
            }
            val id = repository.insertClient(client)
            when (id) {
                is Result.Success -> id
                is Result.Error -> id
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

