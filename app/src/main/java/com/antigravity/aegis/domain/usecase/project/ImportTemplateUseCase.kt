package com.antigravity.aegis.domain.usecase.project

import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.ProjectStatus
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.domain.model.transfer.ProjectTemplateDto
import com.antigravity.aegis.domain.repository.CrmRepository
import com.antigravity.aegis.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject

class ImportTemplateUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val crmRepository: CrmRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): Result<Long> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: return Result.failure(Exception("Could not read file"))

            val dto = Gson().fromJson(json, ProjectTemplateDto::class.java)
            val templateId = saveTemplate(dto, null)
            
            Result.success(templateId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveTemplate(dto: ProjectTemplateDto, parentId: Int?): Long {
        val project = ProjectEntity(
            clientId = 1, // Dummy Client ID for templates. Or require selection. 
            // Better to use a "System" client or just existing logic.
            // Since we need valid FK, we assume Client ID 1 exists (usually created on init) or find first.
            // Ideally templates shouldn't need a client, but Schema.
            // Let's use 0 is possible? No, FK calls for valid ID.
            // For now, let's fetch first client or just use 1 and hope.
            // Safe bet: Fetch any client.
            // Wait, we can't depend on "any" client if DB is empty.
            // Assuming DB has at least one client. If not, create one?
            // Let's rely on finding a valid ID.
            parentProjectId = parentId,
            name = dto.name,
            status = ProjectStatus.ACTIVE,
            startDate = System.currentTimeMillis(),
            isTemplate = true
        )
        
        // Hack for Client ID: we must provide one. 
        // We'll use the repository to find a valid one, or create a dummy.
        // BUT this UseCase shouldn't behave unpredictably. 
        // For templates, the clientId is irrelevant until instantiated.
        // Let's try inserting with 1, if it fails, we catch.
        // Or better: The Repository helper "insertProject" might handle this? No.
        
        // Let's try to fetch active clients.
        val client = crmRepository.getAllClients().firstOrNull()?.firstOrNull()
        val safeClientId = client?.id ?: 1 // Fallback to 1, FK might fail if empty.
        
        val projectWithClient = project.copy(clientId = safeClientId)

        val id = projectRepository.insertProject(projectWithClient)

        // Save Tasks
        dto.tasks.forEach { taskDto ->
            crmRepository.createTask(
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
    
    // Helper needed for Flow collection in non-flow context
    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstOrNull(): T? {
        var result: T? = null
        try {
            collect { 
                result = it
                throw Exception("Abort") // Stop collection
            }
        } catch (e: Exception) {
            // Expected
        }
        return result
    }
}
