package com.antigravity.aegis.data.di

import com.antigravity.aegis.data.local.AegisDatabase
import com.antigravity.aegis.data.local.dao.ClientDao
import com.antigravity.aegis.data.repository.ClientRepositoryImpl
import com.antigravity.aegis.domain.repository.ClientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    @Singleton
    fun provideClientRepository(clientDao: ClientDao): ClientRepository {
        return ClientRepositoryImpl(clientDao)
    }
}
