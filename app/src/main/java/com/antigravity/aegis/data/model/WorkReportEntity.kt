package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_reports",
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
data class WorkReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val date: Long,
    val description: String,
    val signaturePath: String? = null,
    val photoPaths: String? = null // Storing as JSON string or comma separated for simplicity in this iteration
)
