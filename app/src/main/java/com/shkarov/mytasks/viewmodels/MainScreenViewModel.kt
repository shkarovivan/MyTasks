package com.shkarov.mytasks.viewmodels

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.domain.model.Work
import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.data.ApiResponse
import com.shkarov.mytasks.network.data.ChatMessage
import com.shkarov.mytasks.network.data.ChatRequest
import com.shkarov.mytasks.network.data.TaskResponse
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.utils.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Locale.getDefault
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val application: Application,
    private val repository: TasksRepository,
    private val apiService: ApiService,
) : AndroidViewModel(application) {

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: MutableStateFlow<Boolean> = _loading
    private val _searchResultFlow: MutableStateFlow<SearchResult?> = MutableStateFlow(null)
    val searchResultFlow: StateFlow<SearchResult?> = _searchResultFlow.asStateFlow()

    private val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTaskRequest(request: String, isWorkTask: Boolean) {
        viewModelScope.launch {
            val chatResponse = sendRequest(createNewTaskRequest(request))
            if (chatResponse.isSuccessful) {
                val rawJson = extractJsonFromContent(chatResponse.responseString)
                val gson = Gson()
                val taskResponse: TaskResponse =
                    gson.fromJson(rawJson, TaskResponse::class.java)
                val task = taskResponseToTask(taskResponse = taskResponse, isWorkTask = isWorkTask)
                repository.insertTask(task = task)

                Timber.d("$TAG Получена задача: $task")
            }
        }
    }

    fun searchRequest(request: String, isWorkTask: Boolean) {
        viewModelScope.launch {
            val work = (if (isWorkTask) Work.WORK.name else Work.HOME.name).lowercase(getDefault())
            val tasks = repository.getTaskByWork(work = work)
            val prompt = createSearchRequest(request, tasks)

            Timber.d("$TAG prompt - $prompt")
            val response = sendRequest(createSearchRequest(request, tasks))
            Timber.d("$TAG searchResponse - $response")
            _searchResultFlow.value = extractSearchRequest(response).also {
                Timber.d("$TAG searchResults2- $it")
            }

        }
    }

    private fun extractSearchRequest(chatResponse: ChatResponse): SearchResult? {
        return if (chatResponse.isSuccessful) {
            val rawJson = extractJsonFromContent(chatResponse.responseString)
            val gson = Gson()
            val searchResult: SearchResult =
                gson.fromJson(rawJson, SearchResult::class.java)
            searchResult
        } else {
            null
        }
    }

    private suspend fun sendRequest(request: String): ChatResponse {
        var result = ChatResponse(false, "")
        try {
            _loading.value = true
            val response = apiService.chatRequest(
                ChatRequest(
                    messages = listOf(
                        ChatMessage(
                            content = createNewTaskRequest(request)
                        )
                    )
                )
            )
            if (response.isSuccessful) {
                val apiResponse: ApiResponse = response.body() ?: return ChatResponse(false, "")
                val content = apiResponse.choices[0].message.content
                result = ChatResponse(true, content)
            }
            _loading.value = false
        } catch (e: Exception) {
            Timber.e("$TAG Ошибка запроса: ${e.message}")
            result = ChatResponse(false, e.message ?: "")
        } finally {
            _loading.value = false
        }
        return result
    }

    private fun extractJsonFromContent(content: String): String {
        return content
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNewTaskRequest(request: String): String {
        return application.getString(R.string.prompt_add_task_date) +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                application.getString(R.string.prompt_add_task_start) +
                request +
                application.getString(R.string.prompt_add_task_end)
    }

    private fun createSearchRequest(request: String, tasks: List<Task>): String {
        val staticPrompt = application.getString(R.string.task_search_prompt_static).trim()
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

    private fun taskResponseToTask(taskResponse: TaskResponse, isWorkTask: Boolean): Task {
        return Task(
            id = System.currentTimeMillis().toString(),
            created = SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).format(Date()),
            title = taskResponse.title,
            description = taskResponse.description,
            type = when (taskResponse.type) {
                "DAILY" -> Type.DAILY.value
                "MEDIUM" -> Type.MEDIUM.value
                "LARGE" -> Type.LARGE.value
                else -> Type.DAILY.value
            },
            deadLine = taskResponse.date,
            deadLineMs = taskResponse.date.toEpochMillis(),
            status = Status.STARTED,
            work = if (isWorkTask) Work.WORK else Work.HOME
        )
    }

    data class ChatResponse(
        val isSuccessful: Boolean,
        val responseString: String
    )

    companion object {
        private const val TAG = "MainScreenViewModel"
    }
}