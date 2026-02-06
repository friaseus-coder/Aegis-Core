package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de repositorio para operaciones con Tareas.
 * Extraído del monolito CrmRepository para cumplir con SRP.
 */
interface TaskRepository {
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun getTaskById(id: Int): TaskEntity?
    fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>>
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)
    suspend fun updateTaskStatusString(taskId: Int, status: String)
}
