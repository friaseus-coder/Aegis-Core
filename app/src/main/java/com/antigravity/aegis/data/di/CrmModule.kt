package com.antigravity.aegis.data.di

import com.antigravity.aegis.data.repository.CrmRepositoryImpl
import com.antigravity.aegis.domain.repository.CrmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrmModule {

    @Binds
    @Singleton
    abstract fun bindCrmRepository(
        crmRepositoryImpl: CrmRepositoryImpl
    ): CrmRepository
}
