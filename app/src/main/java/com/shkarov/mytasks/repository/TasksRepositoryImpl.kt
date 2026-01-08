package com.shkarov.mytasks.repository

import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.data_base.TasksDbDao
import timber.log.Timber
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val tasksDao: TasksDbDao
) : TasksRepository {

    override suspend fun getTaskByType(type: String): List<Task> {
        return tasksDao.getTaskByType(type).also {
            Timber.d("tasks2 - $it")
        }
    }

    override suspend fun getTaskByWork(work: String): List<Task> {
        return tasksDao.getTaskByWork(work)
    }

    override suspend fun getTaskByStatus(status: String): List<Task> {
        return tasksDao.getTaskByStatus(status)
    }

    override suspend fun insertTask(task: Task) {
        tasksDao.insertTask(task)
    }

    override suspend fun getAllTasks(): List<Task> {
        return try {
            Timber.d("getAllTasks: starting query")
            val tasks = tasksDao.getAllTasks()
            Timber.d("getAllTasks: found ${tasks.size} tasks")
            tasks.forEach { task ->
                Timber.d("getAllTasks: task id=${task.id}, type=${task.type}, status=${task.status}")
            }
            tasks
        } catch (e: Exception) {
            Timber.e(e, "getAllTasks: error occurred")
            emptyList()
        }
    }

    override suspend fun deleteTaskByID(id: String) {
        tasksDao.deleteTaskByID(id)
    }

    override suspend fun deleteAllTasks() {
        tasksDao.deleteAllTasks()
    }
}