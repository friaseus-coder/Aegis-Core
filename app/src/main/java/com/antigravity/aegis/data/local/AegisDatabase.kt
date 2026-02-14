package com.antigravity.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.entity.UserEntity
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.WorkReportEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.data.local.entity.MileageLogEntity
import com.antigravity.aegis.data.local.entity.DocumentEntity
import com.antigravity.aegis.data.local.dao.DocumentDao
import com.antigravity.aegis.data.local.dao.UserConfigDao
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.BudgetLogEntity
import com.antigravity.aegis.data.local.dao.ProjectDao
import com.antigravity.aegis.data.local.dao.BudgetDao
import com.antigravity.aegis.data.local.dao.ExpenseDao
import com.antigravity.aegis.data.local.dao.TaskDao
import com.antigravity.aegis.data.local.entity.UserConfig

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
    version = 25,
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
    companion object {
        val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE clients ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE projects ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE expenses ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Agregar campos para personalización de módulos en UserConfig
                database.execSQL("ALTER TABLE user_config ADD COLUMN moduleOrder TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE user_config ADD COLUMN hiddenModules TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_20_21 = object : androidx.room.migration.Migration(20, 21) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE projects ADD COLUMN parentProjectId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_21_22 = object : androidx.room.migration.Migration(21, 22) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE projects ADD COLUMN isTemplate INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_22_23 = object : androidx.room.migration.Migration(22, 23) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE projects ADD COLUMN category TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tasks ADD COLUMN estimatedDuration INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_23_24 = object : androidx.room.migration.Migration(23, 24) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Add description column to existing table (needed for data copy)
                database.execSQL("ALTER TABLE projects ADD COLUMN description TEXT DEFAULT NULL")
                // 2. Rename old table
                database.execSQL("ALTER TABLE projects RENAME TO projects_old")
                // 3. Drop old indices (they reference old table)
                database.execSQL("DROP INDEX IF EXISTS index_projects_clientId")
                database.execSQL("DROP INDEX IF EXISTS index_projects_parentProjectId")
                // 4. Create new table with final name so self-referencing FK is correct
                database.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientId INTEGER NOT NULL, parentProjectId INTEGER DEFAULT NULL, name TEXT NOT NULL, status TEXT NOT NULL, startDate INTEGER NOT NULL, endDate INTEGER DEFAULT NULL, isArchived INTEGER NOT NULL DEFAULT 0, isSynced INTEGER NOT NULL DEFAULT 0, isTemplate INTEGER NOT NULL DEFAULT 0, category TEXT DEFAULT NULL, description TEXT DEFAULT NULL, FOREIGN KEY (clientId) REFERENCES clients(id) ON DELETE CASCADE, FOREIGN KEY (parentProjectId) REFERENCES projects(id) ON DELETE CASCADE)")
                // 5. Copy data
                database.execSQL("INSERT INTO projects (id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description) SELECT id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description FROM projects_old")
                // 6. Drop old table
                database.execSQL("DROP TABLE projects_old")
                // 7. Recreate indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_clientId ON projects(clientId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_parentProjectId ON projects(parentProjectId)")
            }
        }

        val MIGRATION_24_25 = object : androidx.room.migration.Migration(24, 25) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Fix for users who ran the broken v24 migration.
                // Recreate projects table with correct self-referencing FK.
                database.execSQL("ALTER TABLE projects RENAME TO projects_old")
                database.execSQL("DROP INDEX IF EXISTS index_projects_clientId")
                database.execSQL("DROP INDEX IF EXISTS index_projects_parentProjectId")
                database.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientId INTEGER NOT NULL, parentProjectId INTEGER DEFAULT NULL, name TEXT NOT NULL, status TEXT NOT NULL, startDate INTEGER NOT NULL, endDate INTEGER DEFAULT NULL, isArchived INTEGER NOT NULL DEFAULT 0, isSynced INTEGER NOT NULL DEFAULT 0, isTemplate INTEGER NOT NULL DEFAULT 0, category TEXT DEFAULT NULL, description TEXT DEFAULT NULL, FOREIGN KEY (clientId) REFERENCES clients(id) ON DELETE CASCADE, FOREIGN KEY (parentProjectId) REFERENCES projects(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO projects (id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description) SELECT id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description FROM projects_old")
                database.execSQL("DROP TABLE projects_old")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_clientId ON projects(clientId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_parentProjectId ON projects(parentProjectId)")
            }
        }
    }
}
