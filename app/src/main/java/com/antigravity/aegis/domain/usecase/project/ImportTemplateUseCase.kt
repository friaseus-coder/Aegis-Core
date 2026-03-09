package com.antigravity.aegis.domain.usecase.project

import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.model.CrmStatus
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.domain.model.transfer.ProjectTemplateDto
import com.antigravity.aegis.domain.repository.ClientRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.Result
import com.antigravity.aegis.domain.util.getOrNull
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject

class ImportTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val clientRepository: ClientRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): Result<Long> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: return Result.Error(Exception("Could not read file"))

            invokeFromJson(json)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun invokeFromJson(json: String): Result<Long> {
        return try {
            val dto = Gson().fromJson(json, ProjectTemplateDto::class.java)
            val templateId = saveTemplate(dto, null)
            Result.Success(templateId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun saveTemplate(dto: ProjectTemplateDto, parentId: Int?): Long {
        val project = ProjectEntity(
            clientId = 1, // Dummy Client ID for templates. FK requires valid ID.
            parentProjectId = parentId,
            name = dto.name,
            status = CrmStatus.ACTIVE,
            startDate = System.currentTimeMillis(),
            isTemplate = true,
            materials = dto.materials,
            price = dto.price,
            estimatedTime = dto.estimatedTime,
            estimatedTimeUnit = dto.estimatedTimeUnit
        )

        // Try to find a valid client ID; fallback to 1
        val safeClientId = clientRepository.getAllClientsSync().firstOrNull()?.id ?: 1

        val projectWithClient = project.copy(clientId = safeClientId)

        val id = projectRepository.insertProject(projectWithClient).getOrNull()
            ?: throw IllegalStateException("Error al insertar la plantilla")

        // Save Tasks
        dto.tasks.forEach { taskDto ->
            taskRepository.insertTask(
                TaskEntity(
                    projectId = id.toInt(),
                    title = taskDto.title,
                    description = taskDto.description
                )
            )
        }

        // Save Subprojects
        dto.subProjects.forEach { subDto ->
            saveTemplate(subDto, id.toInt())
        }

        return id
    }
}

