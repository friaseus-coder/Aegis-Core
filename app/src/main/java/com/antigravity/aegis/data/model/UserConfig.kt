package com.antigravity.aegis.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfig(
    @PrimaryKey val id: Int = 1, // Single row table
    @ColumnInfo(name = "pin_hash") val pinHash: String?,
    @ColumnInfo(name = "biometric_enabled") val biometricEnabled: Boolean = false,
    @ColumnInfo(name = "price_per_km") val pricePerKm: Double = 0.0
)
