package com.antigravity.aegis.data.di

import com.antigravity.aegis.data.repository.TaskRepositoryImpl
import com.antigravity.aegis.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para inyección de dependencias de Tareas.
 * Vincula la implementación TaskRepositoryImpl con la interfaz TaskRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TaskModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}
