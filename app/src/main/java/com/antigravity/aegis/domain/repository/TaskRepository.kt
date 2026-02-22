package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de repositorio para operaciones con Tareas.
 * Extraído del monolito CrmRepository para cumplir con SRP.
 */
interface TaskRepository {
    suspend fun insertTask(task: TaskEntity): Result<Long>
    suspend fun updateTask(task: TaskEntity): Result<Unit>
    suspend fun deleteTask(task: TaskEntity): Result<Unit>
    suspend fun getTaskById(id: Int): TaskEntity?
    fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>>
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean): Result<Unit>
    suspend fun updateTaskStatusString(taskId: Int, status: String): Result<Unit>
}

