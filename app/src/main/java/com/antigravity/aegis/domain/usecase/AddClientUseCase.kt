package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.repository.ClientRepository
import javax.inject.Inject

class AddClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(client: Client): Result<Int> {
        return try {
            if (client.firstName.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }
            // Add more validation logic here if needed
            val id = repository.insertClient(client)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
