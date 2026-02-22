package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.ProjectDao
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.relation.ProjectWithSubProjects
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao
) : ProjectRepository {

    override suspend fun insertProject(project: ProjectEntity): Result<Long> = try {
        Result.Success(dao.insertProject(project))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateProject(project: ProjectEntity): Result<Unit> = try {
        Result.Success(dao.updateProject(project))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getProjectById(id: Int): ProjectEntity? = dao.getProjectById(id)
    override fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()
    override fun getActiveProjects(): Flow<List<ProjectEntity>> = dao.getActiveProjects()
    override fun getActiveRootProjects(): Flow<List<ProjectEntity>> = dao.getActiveRootProjects()
    override fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>> = dao.getProjectsByStatus(status)
    override fun getProjectsByClient(clientId: Int): Flow<List<ProjectEntity>> = dao.getProjectsByClient(clientId)

    override suspend fun updateProjectStatus(projectId: Int, status: String): Result<Unit> = try {
        Result.Success(dao.updateProjectStatus(projectId, status))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getProjectsActiveInPeriod(periodStart: Long, periodEnd: Long): List<ProjectEntity> =
        dao.getProjectsActiveInPeriod(periodStart, periodEnd)

    override suspend fun deleteProject(project: ProjectEntity): Result<Unit> = try {
        Result.Success(dao.deleteProject(project))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun getSubProjects(parentId: Int): Flow<List<ProjectEntity>> = dao.getSubProjects(parentId)
    override suspend fun getSubProjectsSync(parentId: Int): List<ProjectEntity> = dao.getSubProjectsSync(parentId)
    override fun getProjectWithSubProjects(id: Int): Flow<ProjectWithSubProjects> = dao.getProjectWithSubProjects(id)
    override suspend fun getProjectWithSubProjectsSync(id: Int): ProjectWithSubProjects? = dao.getProjectWithSubProjectsSync(id)
    override fun getTemplates(): Flow<List<ProjectEntity>> = dao.getTemplates()
    override fun getTemplateCategories(): Flow<List<String>> = dao.getTemplateCategories()
    override fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()
}

