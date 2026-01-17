package com.antigravity.aegis.data.model

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
    val description: String,
    val isCompleted: Boolean = false
)
