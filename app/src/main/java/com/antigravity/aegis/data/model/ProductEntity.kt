package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String,
    val name: String,
    val description: String?,
    val quantity: Int = 0,
    val minQuantity: Int = 5,
    val price: Double = 0.0
)
