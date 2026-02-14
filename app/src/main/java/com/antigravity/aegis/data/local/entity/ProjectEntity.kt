package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.antigravity.aegis.data.local.entity.ClientEntity

enum class ProjectStatus {
    ACTIVE,
    CLOSED,
    ARCHIVED
}

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentProjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("clientId"),
        Index("parentProjectId")
    ]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val parentProjectId: Int? = null, // Para subproyectos
    val name: String,
    val status: ProjectStatus, // "Active", "Closed", "Archived"
    val startDate: Long,
    val endDate: Long? = null,
    val isArchived: Boolean = false,
    val isSynced: Boolean = false,
    val isTemplate: Boolean = false,
    val category: String? = null,
    val description: String? = null
)
