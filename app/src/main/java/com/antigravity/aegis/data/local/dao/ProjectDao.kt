package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY startDate DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = 'Active' ORDER BY startDate DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY startDate DESC")
    fun getProjectsByClient(clientId: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = :status ORDER BY startDate DESC")
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>
    
    @Query("UPDATE projects SET status = :status WHERE id = :projectId")
    suspend fun updateProjectStatus(projectId: Int, status: String)

    @Query("SELECT * FROM projects WHERE startDate <= :periodEnd AND (endDate IS NULL OR endDate >= :periodStart)")
    suspend fun getProjectsActiveInPeriod(periodStart: Long, periodEnd: Long): List<ProjectEntity>
}
