package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.antigravity.aegis.data.local.entity.ClientEntity

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val fileName: String, // Internal encrypted filename
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long
)
