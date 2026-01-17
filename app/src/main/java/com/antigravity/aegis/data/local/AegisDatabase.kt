package com.antigravity.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aegis.data.local.dao.UserConfigDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.model.UserConfig
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.data.model.ExpenseEntity
import com.antigravity.aegis.data.model.ProductEntity
import com.antigravity.aegis.data.model.MileageLogEntity

@Database(
    entities = [
        UserConfig::class,
        ClientEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        WorkReportEntity::class,
        QuoteEntity::class,
        ExpenseEntity::class,
        ProductEntity::class,
        MileageLogEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun userConfigDao(): UserConfigDao
    abstract fun crmDao(): CrmDao
}
