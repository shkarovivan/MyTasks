package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.shkarov.mytasks.R
import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.data.ApiResponse
import com.shkarov.mytasks.network.data.ChatMessage
import com.shkarov.mytasks.network.data.ChatRequest
import com.shkarov.mytasks.network.data.TaskResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val application: Application,
//    private val repository: TasksRepository,
    private val apiService: ApiService,
) : AndroidViewModel(application) {

    fun saveTaskRequest(request: String) {
        viewModelScope.launch {
            try {
                val response = apiService.chatRequest(
                    ChatRequest(
                        messages = listOf(
                            ChatMessage(
                                content = createRequest(request)
                            )
                        )
                    )
                )
                if (response.isSuccessful) {
                    val apiResponse: ApiResponse = response.body() ?: return@launch
                    val content = apiResponse.choices[0].message.content

                    val rawJson = extractJsonFromContent(content)
                    val gson = Gson()
                    val task: TaskResponse = gson.fromJson(rawJson, TaskResponse::class.java)

                    Timber.d("$TAG Получена задача: $task")
                }
            } catch (e: Exception) {
                Timber.e("$TAG Ошибка запроса: ${e.message}")
            }
        }
    }

    private fun extractJsonFromContent(content: String): String {
        return content
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    private fun createRequest(request: String): String {
        return application.getString(R.string.prompt_start) +
                request +
                application.getString(R.string.prompt_date) +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                application.getString(R.string.prompt_end)
    }

    companion object {
        private const val TAG = "MainScreenViewModel"
    }
}