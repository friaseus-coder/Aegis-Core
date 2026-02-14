package com.antigravity.aegis.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.antigravity.aegis.data.local.entity.ProjectEntity

data class ProjectWithSubProjects(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentProjectId"
    )
    val subProjects: List<ProjectEntity>
)
