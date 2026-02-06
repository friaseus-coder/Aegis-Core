package com.antigravity.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.model.UserEntity
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.TaskEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import com.antigravity.aegis.data.model.ExpenseEntity
import com.antigravity.aegis.data.model.ProductEntity
import com.antigravity.aegis.data.model.MileageLogEntity
import com.antigravity.aegis.data.model.DocumentEntity
import com.antigravity.aegis.data.local.dao.DocumentDao
import com.antigravity.aegis.data.local.dao.UserConfigDao
import com.antigravity.aegis.data.model.BudgetLineEntity
import com.antigravity.aegis.data.model.BudgetLogEntity
import com.antigravity.aegis.data.local.dao.ProjectDao
import com.antigravity.aegis.data.local.dao.BudgetDao
import com.antigravity.aegis.data.local.dao.ExpenseDao
import com.antigravity.aegis.data.local.dao.TaskDao
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
        UserConfig::class,
        DocumentEntity::class,
        BudgetLineEntity::class,
        BudgetLogEntity::class
    ],
    version = 18,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun userEntityDao(): UserEntityDao
    abstract fun clientDao(): com.antigravity.aegis.data.local.dao.ClientDao
    abstract fun crmDao(): CrmDao
    abstract fun userConfigDao(): UserConfigDao
    abstract fun documentDao(): DocumentDao
    abstract fun projectDao(): ProjectDao
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun taskDao(): TaskDao
}
