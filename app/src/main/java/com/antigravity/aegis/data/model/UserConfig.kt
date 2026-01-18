package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfig(
    @PrimaryKey val id: Int = 1,
    val pinHash: String? = null,
    val biometricsEnabled: Boolean = false,
    val pricePerKm: Double = 0.35 // Default mileage price
)
