package com.shkarov.mytasks.data_base

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shkarov.mytasks.data.Task

@Database(
    entities = [Task::class],
    version = TasksDataBase.DB_VERSION,
    exportSchema = true
)
abstract class TasksDataBase : RoomDatabase()  {
    abstract fun taskDbDao(): TasksDbDao

    companion object{
        const val DB_VERSION = 1
        const val DB_NAME = "tasks-database"
    }
}
