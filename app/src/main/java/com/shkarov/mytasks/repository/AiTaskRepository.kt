package com.shkarov.mytasks.repository

import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.network.data.TaskResponse

// Abstraction over the two AI connection modes:
// direct provider access and the MyTasksBackend proxy.
interface AiTaskRepository {

    // Sends a free-form task description, returns the LLM-parsed task fields.
    suspend fun sendTaskRequest(request: String): TaskResponse?

    // Sends a user search request with the tasks to search in.
    suspend fun sendSearchRequest(request: String, tasks: List<Task>): SearchResult?
}
