package com.shkarov.mytasks.data_base

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shkarov.mytasks.data.Task

@Dao
interface TasksDbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.TYPE} = :type")
    suspend fun getTaskByType(type: String): List<Task>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.STATUS} = :status")
    suspend fun getTaskByStatus(status: String):List<Task>

    @Query("DELETE FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.ID} = :id")
    suspend fun deleteTaskByID(id: String)

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME}")
    suspend fun getAllTasks(): List<Task>

    @Query("DELETE FROM ${TaskDataBaseContract.TABLE_NAME}")
    suspend fun deleteAllTasks()
}

