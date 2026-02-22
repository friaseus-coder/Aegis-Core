package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.TaskDao
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de Tareas.
 * Extraído del monolito CrmRepositoryImpl para cumplir con SRP.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(task: TaskEntity): Result<Long> = try {
        Result.Success(taskDao.insertTask(task))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateTask(task: TaskEntity): Result<Unit> = try {
        Result.Success(taskDao.updateTask(task))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteTask(task: TaskEntity): Result<Unit> = try {
        Result.Success(taskDao.deleteTask(task))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getTaskById(id: Int): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    override fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>> {
        return taskDao.getTasksByProject(projectId)
    }

    override fun getAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks()
    }

    override suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean): Result<Unit> = try {
        Result.Success(taskDao.updateTaskStatus(taskId, isCompleted))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateTaskStatusString(taskId: Int, status: String): Result<Unit> = try {
        Result.Success(taskDao.updateTaskStatusString(taskId, status))
    } catch (e: Exception) {
        Result.Error(e)
    }
}

