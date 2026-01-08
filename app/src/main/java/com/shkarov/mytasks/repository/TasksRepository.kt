package com.shkarov.mytasks.repository

import com.shkarov.mytasks.domain.model.Task

interface TasksRepository {
    suspend fun getTaskByType(type: String): List<Task>
    suspend fun getTaskByStatus(status: String): List<Task>
    suspend fun getTaskByWork(work: String): List<Task>
    suspend fun insertTask(task: Task)
    suspend fun deleteTaskByID(id: String)
    suspend fun getAllTasks(): List<Task>
    suspend fun deleteAllTasks()
}