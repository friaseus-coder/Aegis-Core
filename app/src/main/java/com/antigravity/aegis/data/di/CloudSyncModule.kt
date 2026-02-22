package com.antigravity.aegis.data.di

import android.content.Context
import com.antigravity.aegis.data.cloud.GoogleDriveSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudSyncModule {

    @Provides
    @Singleton
    fun provideGoogleDriveSyncManager(
        @ApplicationContext context: Context
    ): GoogleDriveSyncManager {
        return GoogleDriveSyncManager(context)
    }
}
