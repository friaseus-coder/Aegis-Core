package com.antigravity.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
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
import com.antigravity.aegis.data.local.entity.PasswordEntity

@Database(
    entities = [
        ClientEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        QuoteEntity::class,
        ExpenseEntity::class,
        ProductEntity::class,
        MileageLogEntity::class,
        UserConfig::class,
        DocumentEntity::class,
        BudgetLineEntity::class,
        BudgetLogEntity::class,
        PasswordEntity::class,
        com.antigravity.aegis.data.local.entity.SessionEntity::class
    ],
    version = 34,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun clientDao(): com.antigravity.aegis.data.local.dao.ClientDao
    abstract fun crmDao(): CrmDao
    abstract fun userConfigDao(): UserConfigDao
    abstract fun documentDao(): DocumentDao
    abstract fun projectDao(): ProjectDao
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun taskDao(): TaskDao
    abstract fun passwordDao(): com.antigravity.aegis.data.local.dao.PasswordDao
    abstract fun sessionDao(): com.antigravity.aegis.data.local.dao.SessionDao
    
    companion object {
        val MIGRATION_33_34 = object : androidx.room.migration.Migration(33, 34) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, projectId INTEGER NOT NULL, date INTEGER NOT NULL, location TEXT NOT NULL, duration TEXT NOT NULL, notes TEXT NOT NULL, exercises TEXT NOT NULL, nextSessionDate INTEGER, googleCalendarEventId TEXT, FOREIGN KEY (projectId) REFERENCES projects(id) ON DELETE CASCADE)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_projectId ON sessions(projectId)")
            }
        }

        val MIGRATION_32_33 = object : androidx.room.migration.Migration(32, 33) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE clients ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE projects ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tasks ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE expenses ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE quotes ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE quotes ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE mileage_logs ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE mileage_logs ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_31_32 = object : androidx.room.migration.Migration(31, 32) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN baseAmount REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE expenses ADD COLUMN taxAmount REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE user_config ADD COLUMN defaultTaxPercent REAL NOT NULL DEFAULT 21.0")
            }
        }

        val MIGRATION_30_31 = object : androidx.room.migration.Migration(30, 31) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_config ADD COLUMN currency TEXT NOT NULL DEFAULT 'EUR'")
            }
        }

        val MIGRATION_29_30 = object : androidx.room.migration.Migration(29, 30) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Drop users table as we migrated to OS-Level authentication
                database.execSQL("DROP TABLE IF EXISTS users")
            }
        }

        val MIGRATION_28_29 = object : androidx.room.migration.Migration(28, 29) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Drop email and phone columns by recreating the table
                database.execSQL("ALTER TABLE users RENAME TO users_old")
                database.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, role TEXT NOT NULL, pin_hash TEXT, biometric_enabled INTEGER NOT NULL, price_per_km REAL NOT NULL, language TEXT NOT NULL, force_pin_change INTEGER NOT NULL)")
                database.execSQL("INSERT INTO users (id, name, role, pin_hash, biometric_enabled, price_per_km, language, force_pin_change) SELECT id, name, role, pin_hash, biometric_enabled, price_per_km, language, force_pin_change FROM users_old")
                database.execSQL("DROP TABLE users_old")
            }
        }

        val MIGRATION_27_28 = object : androidx.room.migration.Migration(27, 28) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS passwords (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, username TEXT NOT NULL, encryptedPassword TEXT NOT NULL, website TEXT, notes TEXT, lastModified INTEGER NOT NULL)")
            }
        }
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

        val MIGRATION_25_26 = object : androidx.room.migration.Migration(25, 26) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Make clientId Nullable
                database.execSQL("ALTER TABLE projects RENAME TO projects_old")
                database.execSQL("DROP INDEX IF EXISTS index_projects_clientId")
                database.execSQL("DROP INDEX IF EXISTS index_projects_parentProjectId")
                // Note: clientId is now INTEGER (nullable) instead of INTEGER NOT NULL
                database.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientId INTEGER, parentProjectId INTEGER DEFAULT NULL, name TEXT NOT NULL, status TEXT NOT NULL, startDate INTEGER NOT NULL, endDate INTEGER DEFAULT NULL, isArchived INTEGER NOT NULL DEFAULT 0, isSynced INTEGER NOT NULL DEFAULT 0, isTemplate INTEGER NOT NULL DEFAULT 0, category TEXT DEFAULT NULL, description TEXT DEFAULT NULL, FOREIGN KEY (clientId) REFERENCES clients(id) ON DELETE CASCADE, FOREIGN KEY (parentProjectId) REFERENCES projects(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO projects (id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description) SELECT id, clientId, parentProjectId, name, status, startDate, endDate, isArchived, isSynced, isTemplate, category, description FROM projects_old")
                database.execSQL("DROP TABLE projects_old")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_clientId ON projects(clientId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_projects_parentProjectId ON projects(parentProjectId)")
            }
        }

        val MIGRATION_26_27 = object : androidx.room.migration.Migration(26, 27) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Añadir campos avanzados a Subproyectos
                database.execSQL("ALTER TABLE projects ADD COLUMN materials TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE projects ADD COLUMN price REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE projects ADD COLUMN estimatedTime REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE projects ADD COLUMN estimatedTimeUnit TEXT DEFAULT NULL")
            }
        }
    }
}
