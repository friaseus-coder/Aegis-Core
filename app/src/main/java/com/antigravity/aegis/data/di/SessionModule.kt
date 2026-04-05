package com.antigravity.aegis.data.di

import com.antigravity.aegis.data.repository.SessionRepositoryImpl
import com.antigravity.aegis.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository
}
