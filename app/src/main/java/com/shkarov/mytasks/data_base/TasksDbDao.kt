package com.shkarov.mytasks.data_base

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shkarov.mytasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDbDao {
    // UI queries hide tombstones (deleted = 0); sync-only methods below see all rows.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.TYPE} = :type AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTaskByType(type: String): List<Task>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.WORK} = :work AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTaskByWork(work: String): List<Task>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.ID} = :id AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTaskById(id: String): Task

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.ID} = :id AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    fun getTaskByIdFlow(id: String): Flow<Task?>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.STATUS} = :status AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTaskByStatus(status: String): List<Task>

    @Query("DELETE FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.ID} = :id")
    suspend fun deleteTaskByID(id: String)

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.DELETED} = 0")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.DEAD_LINE_MS} < :timestamp AND ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTimedTasks(timestamp: Long): List<Task>

    @Query("DELETE FROM ${TaskDataBaseContract.TABLE_NAME}")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.DELETED} = 0")
    suspend fun getTaskCount(): Int

    // --- sync support (used by the sync engine in the next migration stage) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Query("SELECT * FROM ${TaskDataBaseContract.TABLE_NAME} WHERE ${TaskDataBaseContract.Columns.UPDATED_AT} > :updatedAt")
    suspend fun getTasksChangedSince(updatedAt: Long): List<Task>

    @Query("SELECT MAX(${TaskDataBaseContract.Columns.UPDATED_AT}) FROM ${TaskDataBaseContract.TABLE_NAME}")
    suspend fun getMaxUpdatedAt(): Long?
}
