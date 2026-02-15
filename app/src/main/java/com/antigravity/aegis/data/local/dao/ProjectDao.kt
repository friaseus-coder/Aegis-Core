package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

import com.antigravity.aegis.data.local.relation.ProjectWithTasks
import com.antigravity.aegis.data.local.relation.ProjectWithSubProjects

@Dao
interface ProjectDao {
    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithTasks(projectId: Int): Flow<ProjectWithTasks>


    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithSubProjects(projectId: Int): Flow<ProjectWithSubProjects>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectWithSubProjectsSync(projectId: Int): ProjectWithSubProjects?

    @Transaction
    @Query("SELECT * FROM projects WHERE parentProjectId = :parentId")
    fun getSubProjects(parentId: Int): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isTemplate = 0 ORDER BY startDate DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isTemplate = 1 ORDER BY name ASC")
    fun getTemplates(): Flow<List<ProjectEntity>>

    @Query("SELECT DISTINCT category FROM projects WHERE isTemplate = 1 AND category IS NOT NULL ORDER BY category ASC")
    fun getTemplateCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM projects WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM projects WHERE isTemplate = 1 AND name = :name LIMIT 1")
    suspend fun getTemplateByName(name: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE status = 'Active' AND isTemplate = 0 ORDER BY startDate DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE clientId = :clientId AND isTemplate = 0 ORDER BY startDate DESC")
    fun getProjectsByClient(clientId: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = :status AND isTemplate = 0 ORDER BY startDate DESC")
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>
    
    @Query("UPDATE projects SET status = :status WHERE id = :projectId")
    suspend fun updateProjectStatus(projectId: Int, status: String)

    @Query("SELECT * FROM projects WHERE startDate <= :periodEnd AND (endDate IS NULL OR endDate >= :periodStart)")
    suspend fun getProjectsActiveInPeriod(periodStart: Long, periodEnd: Long): List<ProjectEntity>

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Métodos sync para backup/restore
    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()
}
