package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun insertProject(project: ProjectEntity): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun getProjectById(id: Int): ProjectEntity?
    fun getAllProjects(): Flow<List<ProjectEntity>>
    fun getActiveProjects(): Flow<List<ProjectEntity>>
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>
    fun getProjectsByClient(clientId: Int): Flow<List<ProjectEntity>>
    suspend fun updateProjectStatus(projectId: Int, status: String)
    suspend fun getProjectsActiveInPeriod(periodStart: Long, periodEnd: Long): List<ProjectEntity>
    suspend fun deleteProject(project: ProjectEntity)
}
