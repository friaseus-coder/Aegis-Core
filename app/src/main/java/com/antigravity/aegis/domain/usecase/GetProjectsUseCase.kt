package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(filterActive: Boolean = true): Flow<List<ProjectEntity>> {
        return if (filterActive) {
            repository.getActiveProjects()
        } else {
            repository.getAllProjects()
        }
    }
}
