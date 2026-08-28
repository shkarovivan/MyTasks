package com.shkarov.mytasks.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.data.ApiResponse
import com.shkarov.mytasks.network.data.ChatMessage
import com.shkarov.mytasks.network.data.ChatRequest
import com.shkarov.mytasks.network.data.TaskResponse
import com.shkarov.mytasks.settings.SettingsStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// Direct connection to the LLM provider from the app (token in settings).
// Prompt building and LLM output parsing live here, moved from MainScreenViewModel.
@Singleton
class DirectAiTaskRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val settingsStore: SettingsStore
) : AiTaskRepository {

    private val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    override suspend fun sendTaskRequest(request: String): TaskResponse? {
        val content = chat(createNewTaskPrompt(request)) ?: return null
        return try {
            Gson().fromJson(extractJsonFromContent(content), TaskResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e, "$TAG failed to parse task response")
            null
        }
    }

    override suspend fun sendSearchRequest(request: String, tasks: List<Task>): SearchResult? {
        val content = chat(createSearchPrompt(request, tasks)) ?: return null
        return try {
            Gson().fromJson(extractJsonFromContent(content), SearchResult::class.java)
        } catch (e: Exception) {
            Timber.e(e, "$TAG failed to parse search response")
            null
        }
    }

    // Sends the finished prompt as-is. Unlike the old MainScreenViewModel.sendRequest,
    // it never re-wraps the input into the add-task prompt (search prompts used to be
    // sandwiched inside it by mistake).
    private suspend fun chat(prompt: String): String? {
        return try {
            val response = apiService.chatRequest(
                ChatRequest(
                    model = settingsStore.llmModelFlow.first(),
                    messages = listOf(
                        ChatMessage(content = prompt)
                    )
                )
            )
            if (response.isSuccessful) {
                val apiResponse: ApiResponse = response.body() ?: return null
                apiResponse.choices.firstOrNull()?.message?.content
            } else {
                Timber.e("$TAG request failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG request error: ${e.message}")
            null
        }
    }

    private fun createNewTaskPrompt(request: String): String {
        return context.getString(R.string.prompt_add_task_date) +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                context.getString(R.string.prompt_add_task_start) +
                request +
                context.getString(R.string.prompt_add_task_end)
    }

    private fun createSearchPrompt(request: String, tasks: List<Task>): String {
        val staticPrompt = context.getString(R.string.task_search_prompt_static).trim()
        val tasksJson = gson.toJson(tasks)

        return buildString {
            append(staticPrompt)
            append("\n\n")
            append("Запрос пользователя:\n")
            append(request.trim())
            append("\n\n")
            append("Список задач (JSON, массив Task):\n")
            append(tasksJson)
        }
    }

    private fun extractJsonFromContent(content: String): String {
        return content
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    companion object {
        private const val TAG = "DirectAiTaskRepository"
    }
}
