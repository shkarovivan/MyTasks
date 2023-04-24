package com.shkarov.mytasks.di

import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.repository.TasksRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun provideTasksRepository(impl: TasksRepositoryImpl): TasksRepository
}

