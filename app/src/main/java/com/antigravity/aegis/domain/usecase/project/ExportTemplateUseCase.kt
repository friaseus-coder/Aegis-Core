package com.antigravity.aegis.domain.usecase.project

import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.domain.model.transfer.ProjectTemplateDto
import com.antigravity.aegis.domain.model.transfer.TaskTemplateDto
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.antigravity.aegis.domain.repository.TaskRepository
import com.antigravity.aegis.domain.util.Result
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.OutputStreamWriter
import javax.inject.Inject

class ExportTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(templateId: Int, uri: Uri): Result<Unit> {
        return try {
            val project = projectRepository.getProjectById(templateId)
                ?: return Result.Error(Exception("Template not found"))

            if (!project.isTemplate) return Result.Error(Exception("Project is not a template"))

            val dto = buildTemplateDto(project)
            val json = Gson().toJson(dto)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun buildTemplateDto(project: ProjectEntity): ProjectTemplateDto {
        val tasks = taskRepository.getTasksByProject(project.id).firstOrNull() ?: emptyList()
        val subProjects = projectRepository.getSubProjects(project.id).firstOrNull() ?: emptyList()

        val taskDtos = tasks.map { TaskTemplateDto(it.title, it.description) }
        val subProjectDtos = subProjects.map { buildTemplateDto(it) }

        return ProjectTemplateDto(
            name = project.name,
            tasks = taskDtos,
            subProjects = subProjectDtos
        )
    }
}

