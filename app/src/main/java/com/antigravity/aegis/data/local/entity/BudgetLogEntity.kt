package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_logs",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quoteId")]
)
data class BudgetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quoteId: Int,
    val timestamp: Long,
    val action: String, // "SENT", "VIEWED", "ACCEPTED"
    val messageTemplateUsed: String?, // Name of the template used if any
    val channelType: String = "UNKNOWN" // "EMAIL", "WHATSAPP", etc.
)
