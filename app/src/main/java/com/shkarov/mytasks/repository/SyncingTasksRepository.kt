package com.shkarov.mytasks.repository

import com.shkarov.mytasks.data_base.TasksDbDao
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// Decorator over TasksRepositoryImpl: stamps every mutation with updatedAt,
// turns deletes into tombstones and nudges the sync engine. ViewModels and
// screens are unaware of sync; when the user is not logged in the sync
// manager stays passive and behavior is identical to the raw repository.
@Singleton
class SyncingTasksRepository @Inject constructor(
    private val impl: TasksRepositoryImpl,
    private val tasksDao: TasksDbDao,
    private val syncManager: SyncManager
) : TasksRepository {

    override fun getTasksFlow(): Flow<List<Task>> = impl.getTasksFlow()

    override suspend fun getTaskByType(type: String): List<Task> = impl.getTaskByType(type)

    override suspend fun getTaskByStatus(status: String): List<Task> = impl.getTaskByStatus(status)

    override suspend fun getTaskByWork(work: String): List<Task> = impl.getTaskByWork(work)

    override suspend fun getTaskById(id: String): Task = impl.getTaskById(id)

    override fun getTaskByIdFlow(id: String): Flow<Task?> = impl.getTaskByIdFlow(id)

    override suspend fun insertTask(task: Task) {
        impl.insertTask(task.copy(updatedAt = System.currentTimeMillis()))
        syncManager.schedulePush()
    }

    override suspend fun deleteTaskByID(id: String) {
        // Tombstone instead of a hard delete so the deletion replicates and
        // does not resurrect on the next pull/reinstall.
        val task = tasksDao.getTaskByIdIncludingDeleted(id)
        if (task != null && !task.deleted) {
            tasksDao.insertTask(task.copy(deleted = true, updatedAt = System.currentTimeMillis()))
        }
        syncManager.schedulePush()
    }

    override suspend fun getAllTasks(): List<Task> = impl.getAllTasks()

    override suspend fun getTimedTasks(timestamp: Long): List<Task> = impl.getTimedTasks(timestamp)

    override suspend fun deleteAllTasks() {
        tasksDao.markAllDeleted(System.currentTimeMillis())
        syncManager.schedulePush()
    }

    override suspend fun getTaskCount() = impl.getTaskCount()
}
