package com.antigravity.aegis.data.local.dao

import androidx.room.*
import com.antigravity.aegis.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con Tareas (TaskEntity).
 * Extraído del monolito CrmDao para cumplir con SRP.
 */
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY isCompleted ASC")
    fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY projectId ASC, isCompleted ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatusString(taskId: Int, status: String)

    // Métodos sync para backup/restore
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
