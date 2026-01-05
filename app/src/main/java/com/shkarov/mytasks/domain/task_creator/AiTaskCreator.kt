package com.shkarov.mytasks.domain.task_creator

import com.shkarov.mytasks.domain.model.Task

interface AiTaskCreator {
    fun createTask(request: String): Task
}