package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.antigravity.aegis.data.local.entity.ClientEntity

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
    val clientId: Int? = null,
    val parentProjectId: Int? = null, // Para subproyectos
    val name: String,
    val status: String, // Ahora usa CrmStatus constants como "Draft", "Archived", etc.
    val startDate: Long,
    val endDate: Long? = null,
    val isArchived: Boolean = false,
    val isSynced: Boolean = false,
    val isTemplate: Boolean = false,
    val category: String? = null,
    val description: String? = null,
    val materials: String? = null,
    val price: Double? = null,
    val estimatedTime: Double? = null,
    val estimatedTimeUnit: String? = null
)

