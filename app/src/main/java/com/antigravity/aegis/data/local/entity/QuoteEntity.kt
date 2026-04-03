package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int, // Foreign key to Client
    val projectId: Int? = null, // Optional Foreign key to Project
    val date: Long,
    val totalAmount: Double,
    val status: String, // "Draft", "Sent", "Accepted", "Rejected", "Lost"
    val description: String,
    val title: String, // A brief title for the quote
    val calculatedTotal: Long = 0,
    val version: Int = 1,
    val isSynced: Boolean = false,
    val googleCalendarEventId: String? = null
)
