package com.antigravity.aegis.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "role") val role: UserRole,
    @ColumnInfo(name = "pin_hash") val pinHash: String? = null, // Deprecated in favor of SecurityDataSource wrapped keys, but keeping for potential legacy check or non-critical auth
    @ColumnInfo(name = "biometric_enabled") val biometricEnabled: Boolean = false,
    @ColumnInfo(name = "price_per_km") val pricePerKm: Double = 0.0,
    @ColumnInfo(name = "language") val language: String = "es",
    @ColumnInfo(name = "email") val email: String? = null,
    @ColumnInfo(name = "phone") val phone: String? = null,
    @ColumnInfo(name = "force_pin_change") val forcePinChange: Boolean = false
)

enum class UserRole {
    ADMIN, USER, GUEST, MANAGER
}
