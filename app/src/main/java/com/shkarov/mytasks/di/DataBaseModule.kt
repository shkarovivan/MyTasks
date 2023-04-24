package com.shkarov.mytasks.di

import android.content.Context
import androidx.room.Room
import com.shkarov.mytasks.data_base.TasksDataBase
import com.shkarov.mytasks.data_base.TasksDbDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataBaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): TasksDataBase {
        return Room.databaseBuilder(
            context,
            TasksDataBase::class.java,
            TasksDataBase.DB_NAME
        )
            .build()
    }

    @Provides
    fun providesTasksDbDao(db: TasksDataBase): TasksDbDao {
        return db.taskDbDao()
    }
}

