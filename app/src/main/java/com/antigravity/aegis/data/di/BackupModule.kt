package com.antigravity.aegis.data.di

import com.antigravity.aegis.data.repository.BackupRepositoryImpl
import com.antigravity.aegis.domain.repository.BackupRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl
    ): BackupRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()
    }
}
