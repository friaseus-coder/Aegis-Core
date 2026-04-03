package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val totalAmount: Double,
    val merchantName: String?,
    val imagePath: String?, // Path to the saved image file
    val isSynced: Boolean = false,
    val status: String = "Pending", // Verified, Pending, etc.
    val category: String, // e.g., "Material", "Transport", "Office"
    val projectId: Int? = null, // Optional Foreign Key to Project
    val baseAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val googleCalendarEventId: String? = null
)
