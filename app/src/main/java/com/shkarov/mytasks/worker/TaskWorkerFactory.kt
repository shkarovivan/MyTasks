package com.shkarov.mytasks.worker

import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import android.content.Context
import androidx.work.ListenableWorker
import com.shkarov.mytasks.repository.TasksRepository

class TaskWorkerFactory(
    private val repository: TasksRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            TaskReminderWorker::class.java.name ->
                TaskReminderWorker(appContext, workerParameters, repository)
            else -> null
        }
    }
}