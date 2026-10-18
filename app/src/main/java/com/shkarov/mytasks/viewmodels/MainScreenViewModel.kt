package com.shkarov.mytasks.viewmodels

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.domain.model.Work
import com.shkarov.mytasks.network.data.TaskResponse
import com.shkarov.mytasks.repository.AiTaskRepository
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.utils.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale.getDefault
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    application: Application,
    private val repository: TasksRepository,
    private val aiTaskRepository: AiTaskRepository
) : AndroidViewModel(application) {

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: MutableStateFlow<Boolean> = _loading
    private val _searchResultFlow: MutableStateFlow<SearchResult?> = MutableStateFlow(null)
    val searchResultFlow: StateFlow<SearchResult?> = _searchResultFlow.asStateFlow()

    // One-shot error messages (string resource ids) for the UI to show as a toast.
    private val _errorEvent = MutableSharedFlow<Int>()
    val errorEvent: SharedFlow<Int> = _errorEvent.asSharedFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTaskRequest(request: String, isWorkTask: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val taskResponse = runAiRequest { aiTaskRepository.sendTaskRequest(request) }
                if (taskResponse != null) {
                    val task = taskResponseToTask(taskResponse = taskResponse, isWorkTask = isWorkTask)
                    task?.let {
                        repository.insertTask(task = task)
                    }
                    Timber.d("$TAG Получена задача: $task")
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchRequest(request: String, isWorkTask: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val work = (if (isWorkTask) Work.WORK.name else Work.HOME.name).lowercase(getDefault())
                val tasks = repository.getTaskByWork(work = work)
                val searchResult = runAiRequest { aiTaskRepository.sendSearchRequest(request, tasks) }
                _searchResultFlow.value = searchResult.also {
                    Timber.d("$TAG searchResults - $it")
                }
            } finally {
                _loading.value = false
            }
        }
    }

    // Caps any AI request (direct or backend mode) with a hard deadline so the
    // loader can never spin indefinitely, and reports a visible error when the
    // request produced nothing: no connection, timeout, or an HTTP failure.
    private suspend fun <T : Any> runAiRequest(block: suspend () -> T?): T? {
        val result = withTimeoutOrNull(AI_REQUEST_TIMEOUT_MS) { block() }
        if (result == null) {
            _errorEvent.emit(R.string.error_ai_request)
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun taskResponseToTask(taskResponse: TaskResponse, isWorkTask: Boolean): Task? {
        return try {
            Task(
                id = System.currentTimeMillis().toString(),
                created = SimpleDateFormat(
                    "dd.MM.yyyy",
                    getDefault()
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
                deadLineMs = taskResponse.date.toEpochMillis()
                    .takeIf { it > 0L }
                    ?: LocalDate.now()
                        .plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                status = Status.STARTED,
                work = if (isWorkTask) Work.WORK else Work.HOME
            )
        } catch (e: Exception) {
            Timber.e("$TAG обработки TASK  ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "MainScreenViewModel"

        // Hard deadline for one AI request: the backend LLM timeout is 120s
        // server-side, but anything above this is pointless for the UI.
        private const val AI_REQUEST_TIMEOUT_MS = 90_000L
    }
}
