package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val name: String,
    val status: String, // "Active", "Completed", "Archived"
    val deadline: Long? = null
)
