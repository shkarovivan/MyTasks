package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.data.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskScreenViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {
    var errorString = MutableStateFlow<String?>(null)
    var loadProgress = MutableStateFlow(false)
    val dailyTasks = MutableStateFlow<List<Task>>(emptyList())
    val mediumTasks = MutableStateFlow<List<Task>>(emptyList())
    val largeTasks = MutableStateFlow<List<Task>>(emptyList())

    init {
        getDailyTasks()
        getMediumTasks()
        getLargeTasks()
    }

    private fun getDailyTasks() {
        viewModelScope.launch {
            try {
                loadProgress.value = true
                val tasks = withContext(Dispatchers.IO) {
                    Timber.d("getDailyTasks")
//                    repository.getTaskByType(Type.DAILY.value).also {
//                        Timber.d("tasks - $it")
//                    }
                    delay(2000)
                    repository.getAllTasks()
                }
                dailyTasks.value = tasks
            } catch (e: Exception) {
                errorString.value = e.message ?: "Ошибка загрузки ежедневных задач"
            } finally {
                loadProgress.value = false
            }
        }
    }

    private fun getMediumTasks() {
        viewModelScope.launch {
            try {
                loadProgress.value = true
                val tasks = withContext(Dispatchers.IO) {
                    Timber.d("getMediumTasks")
//                    repository.getTaskByType(Type.MEDIUM.value)
                    repository.getAllTasks()
                }
                mediumTasks.value = tasks
            } catch (e: Exception) {
                errorString.value = e.message ?: "Ошибка загрузки средних задач"
            } finally {
                loadProgress.value = false
            }
        }
    }

    private fun getLargeTasks() {
        viewModelScope.launch {
            try {
                loadProgress.value = true
                val tasks = withContext(Dispatchers.IO) {
                    Timber.d("getLargeTasks")
//                    repository.getTaskByType(Type.LARGE.value)
                    repository.getAllTasks()
                }
                largeTasks.value = tasks
            } catch (e: Exception) {
                errorString.value = e.message ?: "Ошибка загрузки крупных задач"
            } finally {
                loadProgress.value = false
            }
        }
    }
}