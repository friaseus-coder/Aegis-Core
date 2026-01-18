package com.antigravity.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.model.UserEntity
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.data.model.ExpenseEntity
import com.antigravity.aegis.data.model.ProductEntity
import com.antigravity.aegis.data.model.MileageLogEntity

import com.antigravity.aegis.data.local.dao.UserConfigDao
import com.antigravity.aegis.data.model.UserConfig

@Database(
    entities = [
        UserEntity::class,
        ClientEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        WorkReportEntity::class,
        QuoteEntity::class,
        ExpenseEntity::class,
        ProductEntity::class,
        MileageLogEntity::class,
        UserConfig::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun userEntityDao(): UserEntityDao
    abstract fun crmDao(): CrmDao
    abstract fun userConfigDao(): UserConfigDao
}
