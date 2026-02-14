package com.antigravity.aegis.domain.usecase.project

import com.antigravity.aegis.data.local.entity.WorkReportEntity
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import javax.inject.Inject

class CreateWorkOrderFromProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository
) {
    suspend operator fun invoke(subProjectId: Int): Long {
        val project = projectRepository.getProjectById(subProjectId)
            ?: throw IllegalArgumentException("Project not found")

        val workReport = WorkReportEntity(
            projectId = project.id,
            date = System.currentTimeMillis(),
            description = "Parte de trabajo: ${project.name}",
            hours = 0.0
        )

        return crmRepository.createWorkReport(workReport)
    }
}
