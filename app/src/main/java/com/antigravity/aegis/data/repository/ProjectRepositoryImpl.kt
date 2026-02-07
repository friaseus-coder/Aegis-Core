package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ProjectDao
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao
) : ProjectRepository {
    override suspend fun insertProject(project: ProjectEntity): Long = dao.insertProject(project)
    override suspend fun updateProject(project: ProjectEntity) = dao.updateProject(project)
    override suspend fun getProjectById(id: Int): ProjectEntity? = dao.getProjectById(id)
    override fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()
    override fun getActiveProjects(): Flow<List<ProjectEntity>> = dao.getActiveProjects()
    override fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>> = dao.getProjectsByStatus(status)
    override fun getProjectsByClient(clientId: Int): Flow<List<ProjectEntity>> = dao.getProjectsByClient(clientId)
    override suspend fun updateProjectStatus(projectId: Int, status: String) = dao.updateProjectStatus(projectId, status)
    override suspend fun getProjectsActiveInPeriod(periodStart: Long, periodEnd: Long): List<ProjectEntity> = dao.getProjectsActiveInPeriod(periodStart, periodEnd)
    override suspend fun deleteProject(project: ProjectEntity) = dao.deleteProject(project)
}
