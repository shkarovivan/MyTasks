package com.shkarov.mytasks.di

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
}

