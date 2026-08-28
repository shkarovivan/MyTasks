package com.shkarov.mytasks.repository

import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.network.data.TaskResponse
import com.shkarov.mytasks.settings.SettingsStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Routes every request to the direct or the backend implementation based on
// the "AI Connection Type" setting; flipping the switch takes effect immediately.
@Singleton
class AiTaskRepositoryImpl @Inject constructor(
    private val directRepository: DirectAiTaskRepository,
    private val backendRepository: BackendAiTaskRepository,
    private val settingsStore: SettingsStore
) : AiTaskRepository {

    private suspend fun current(): AiTaskRepository {
        return if (settingsStore.llmDirectConnectionFlow.first()) {
            directRepository
        } else {
            backendRepository
        }
    }

    override suspend fun sendTaskRequest(request: String): TaskResponse? =
        current().sendTaskRequest(request)

    override suspend fun sendSearchRequest(request: String, tasks: List<Task>): SearchResult? =
        current().sendSearchRequest(request, tasks)
}
