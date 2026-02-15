package com.antigravity.aegis.data.di

import android.content.Context
import androidx.room.Room
import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.data.local.dao.UserEntityDao
import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.dao.DocumentDao
import com.antigravity.aegis.data.security.EncryptionKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
// import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: EncryptionKeyManager
    ): AegisDatabase {
        // NOTE: The key must be set in EncryptionKeyManager BEFORE this method is called.
        // In a real app, you might defer this or use a custom factory that fetches the key on open.
        // For this initialization, we'll assume the key is provided via the Application/Login flow
        // or we handle the "Not Initialized" state gracefully.
        
        // IMPORTANT: We cannot get the key here if it's not set.
        // However, we construct the builder. The factory is usually called when the DB is opened.
        // We pass the keyManager explicitly or a lambda if we want lazy evaluation, 
        // but Standard SupportOpenHelperFactory takes the byte[] directly in constructor.
        
        // If the key is not set (first launch), we might crash or need a default setup.
        // For this task, we assume the user flow ensures setKey() is called.
        
        // TODO: Restaurar SQLCipher con dependencia correcta
        // val passphrase = keyManager.getKey()
        // val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AegisDatabase::class.java,
            "aegis_core.db"
        )
            // .openHelperFactory(factory)
            .fallbackToDestructiveMigration() // Keep this if needed, but we adding migration now
            .addMigrations(
                AegisDatabase.MIGRATION_18_19,
                AegisDatabase.MIGRATION_19_20,
                AegisDatabase.MIGRATION_20_21,
                AegisDatabase.MIGRATION_21_22,
                AegisDatabase.MIGRATION_22_23,
                AegisDatabase.MIGRATION_23_24,
                AegisDatabase.MIGRATION_24_25,
                AegisDatabase.MIGRATION_25_26
            )
            .build()
    }

    @Provides
    fun provideUserEntityDao(database: AegisDatabase): UserEntityDao {
        return database.userEntityDao()
    }

    @Provides
    fun provideCrmDao(database: AegisDatabase): CrmDao {
        return database.crmDao()
    }

    @Provides
    fun provideUserConfigDao(database: AegisDatabase): com.antigravity.aegis.data.local.dao.UserConfigDao {
        return database.userConfigDao()
    }

    @Provides
    fun provideDocumentDao(database: AegisDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideProjectDao(database: AegisDatabase): com.antigravity.aegis.data.local.dao.ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideBudgetDao(database: AegisDatabase): com.antigravity.aegis.data.local.dao.BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideExpenseDao(database: AegisDatabase): com.antigravity.aegis.data.local.dao.ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideTaskDao(database: AegisDatabase): com.antigravity.aegis.data.local.dao.TaskDao {
        return database.taskDao()
    }
}
