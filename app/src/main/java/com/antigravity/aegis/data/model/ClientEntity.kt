package com.antigravity.aegis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val notes: String? = null
)
