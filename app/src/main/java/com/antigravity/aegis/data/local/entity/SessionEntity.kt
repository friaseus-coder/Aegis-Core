package com.antigravity.aegis.data.local.entity

import androidx.room.*

@Entity(
    tableName = "sessions",
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
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val date: Long,
    val location: String,
    val duration: String,
    val notes: String,
    val exercises: String,
    val nextSessionDate: Long? = null,
    val googleCalendarEventId: String? = null
)
