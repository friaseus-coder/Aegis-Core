package com.antigravity.aegis.data.local

import androidx.room.TypeConverter
import com.antigravity.aegis.data.model.ActiveRole
import com.antigravity.aegis.data.model.EntityType

class Converters {
    @TypeConverter
    fun fromEntityType(value: EntityType): String {
        return value.name
    }

    @TypeConverter
    fun toEntityType(value: String): EntityType {
        return try {
            EntityType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EntityType.PARTICULAR // Fallback
        }
    }

    @TypeConverter
    fun fromActiveRole(value: ActiveRole): String {
        return value.name
    }

    @TypeConverter
    fun toActiveRole(value: String): ActiveRole {
        return try {
            ActiveRole.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ActiveRole.AUTONOMO // Fallback
        }
    }
}
