package com.shkarov.mytasks.di

import com.shkarov.mytasks.repository.AiProvidersRepository
import com.shkarov.mytasks.repository.AiProvidersRepositoryImpl
import com.shkarov.mytasks.repository.AiTaskRepository
import com.shkarov.mytasks.repository.AiTaskRepositoryImpl
import com.shkarov.mytasks.repository.AuthRepository
import com.shkarov.mytasks.repository.AuthRepositoryImpl
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.repository.TasksRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideTasksRepository(impl: TasksRepositoryImpl): TasksRepository

    @Binds
    @Singleton
    abstract fun provideAiProviderRepository(impl: AiProvidersRepositoryImpl): AiProvidersRepository

    @Binds
    @Singleton
    abstract fun provideAiTaskRepository(impl: AiTaskRepositoryImpl): AiTaskRepository

    @Binds
    @Singleton
    abstract fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

