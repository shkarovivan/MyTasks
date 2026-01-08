package com.shkarov.mytasks.data_base

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shkarov.mytasks.domain.model.StatusConverter
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.WorkConverter

@Database(
    entities = [Task::class],
    version = TasksDataBase.DB_VERSION,
    exportSchema = true
)
@TypeConverters(
    StatusConverter::class, WorkConverter::class
)
abstract class TasksDataBase : RoomDatabase() {
    abstract fun taskDbDao(): TasksDbDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = TaskDataBaseContract.TABLE_NAME
    }
}
