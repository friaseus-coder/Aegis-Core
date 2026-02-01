package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfig(
    @PrimaryKey val id: Int = 1,
    val pinHash: String? = null,
    val biometricsEnabled: Boolean = false,
    val pricePerKm: Double = 0.35, // Default mileage price
    val language: String = "es", // "es", "en"
    val themeMode: String = "dark", // "system", "light", "dark"
    // New Profile Fields
    val titularName: String = "Usuario",
    val entityType: EntityType = EntityType.PARTICULAR,
    val activeRole: ActiveRole = ActiveRole.AUTONOMO,
    val isDualModeEnabled: Boolean = false,
    val profileImageUri: String? = null,
    
    // Company Details
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPostalCode: String = "",
    val companyCity: String = "",
    val companyProvince: String = "",
    val companyDniCif: String = "",
    val companyLogoUri: String? = null,
    
    // Backup Configuration
    val backupLocationUri: String? = null,
    val lastBackupTimestamp: Long = 0
)

enum class EntityType {
    EMPRESA, PARTICULAR
}

enum class ActiveRole {
    AUTONOMO, TRABAJADOR
}
