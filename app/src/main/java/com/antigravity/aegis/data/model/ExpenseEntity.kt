package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val totalAmount: Double,
    val merchantName: String?,
    val imagePath: String?, // Path to the saved image file
    val status: String = "Pending" // Verified, Pending, etc.
)
