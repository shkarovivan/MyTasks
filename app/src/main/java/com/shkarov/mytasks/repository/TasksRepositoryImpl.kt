package com.shkarov.mytasks.repository

import com.shkarov.mytasks.data_base.TasksDbDao
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val dbDao: TasksDbDao
) : TasksRepository {
}