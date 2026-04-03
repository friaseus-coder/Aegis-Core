package com.antigravity.aegis.data.di

import android.content.Context
import com.antigravity.aegis.data.cloud.GoogleAppsManager
import com.antigravity.aegis.data.cloud.GoogleCalendarSyncManager
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
    fun provideGoogleAppsManager(
        @ApplicationContext context: Context
    ): GoogleAppsManager {
        return GoogleAppsManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleCalendarSyncManager(
        @ApplicationContext context: Context,
        googleAppsManager: GoogleAppsManager
    ): GoogleCalendarSyncManager {
        return GoogleCalendarSyncManager(context, googleAppsManager)
    }
}

