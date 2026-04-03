package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mileage_logs")
data class MileageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val origin: String,
    val destination: String,
    val vehicle: String,
    val startOdometer: Double,
    val endOdometer: Double,
    val distanceKm: Double, // Calculated: End - Start
    val pricePerKmSnapshot: Double, // Price at time of creation
    val calculatedCost: Double, // Calculated: Distance * Price
    val isSynced: Boolean = false,
    val googleCalendarEventId: String? = null
)
