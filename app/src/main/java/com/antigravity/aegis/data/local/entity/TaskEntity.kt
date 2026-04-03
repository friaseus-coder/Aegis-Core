package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false,
    val isActive: Boolean = true,
    val status: String = "Pending", // "Pending", "InProgress", "Completed", "Cancelled"
    val estimatedDuration: Long? = null,
    val googleCalendarEventId: String? = null
)
