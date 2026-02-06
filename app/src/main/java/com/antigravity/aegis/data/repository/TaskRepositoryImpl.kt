package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.TaskDao
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de Tareas.
 * Extraído del monolito CrmRepositoryImpl para cumplir con SRP.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
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

    override suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean) {
        taskDao.updateTaskStatus(taskId, isCompleted)
    }

    override suspend fun updateTaskStatusString(taskId: Int, status: String) {
        taskDao.updateTaskStatusString(taskId, status)
    }
}
