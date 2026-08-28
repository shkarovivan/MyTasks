package com.shkarov.mytasks.repository

import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.network.BackendApi
import com.shkarov.mytasks.network.data.BackendSearchRequest
import com.shkarov.mytasks.network.data.BackendTaskRequest
import com.shkarov.mytasks.network.data.TaskResponse
import com.shkarov.mytasks.settings.SettingsStore
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Connection via the MyTasksBackend server: prompts and parsing are done
// server-side, the provider key never leaves the backend.
@Singleton
class BackendAiTaskRepository @Inject constructor(
    private val backendApi: BackendApi,
    private val settingsStore: SettingsStore
) : AiTaskRepository {

    override suspend fun sendTaskRequest(request: String): TaskResponse? {
        return try {
            val response = backendApi.createTask(
                BackendTaskRequest(
                    text = request,
                    model = settingsStore.llmModelFlow.first()
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                body
            } else {
                Timber.e("$TAG task request failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG task request error: ${e.message}")
            null
        }
    }

    override suspend fun sendSearchRequest(request: String, tasks: List<Task>): SearchResult? {
        return try {
            val response = backendApi.search(
                BackendSearchRequest(
                    request = request,
                    tasks = tasks,
                    model = settingsStore.llmModelFlow.first()
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                SearchResult(
                    request = request,
                    answer = body.answer,
                    ids = ArrayList(body.ids)
                )
            } else {
                Timber.e("$TAG search request failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG search request error: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "BackendAiTaskRepository"
    }
}
