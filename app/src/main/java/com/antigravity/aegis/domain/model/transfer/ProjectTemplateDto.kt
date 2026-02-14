package com.antigravity.aegis.domain.model.transfer

import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity

data class ProjectTemplateDto(
    val name: String,
    val description: String? = null, // Future use
    val tasks: List<TaskTemplateDto>,
    val subProjects: List<ProjectTemplateDto>
)

data class TaskTemplateDto(
    val title: String,
    val description: String
)

// Extension functions to map from Entity to DTO and vice-versa (logic will be in UseCases)
