package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_lines",
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
data class BudgetLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quoteId: Int,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxRate: Double // e.g., 0.21 for 21%
)
