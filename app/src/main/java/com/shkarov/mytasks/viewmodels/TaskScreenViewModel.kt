package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
        updateTasks()
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch{
            repository.deleteTaskByID(taskId)
            updateTasks()
        }
    }

    private fun updateTasks(){
        getDailyTasks()
        getMediumTasks()
        getLargeTasks()
    }

    private fun getDailyTasks() {
        viewModelScope.launch {
            try {
                loadProgress.value = true
                val tasks = withContext(Dispatchers.IO) {
                    Timber.d("getDailyTasks: starting")
                    val allTasks = repository.getAllTasks()
                    Timber.d("getDailyTasks: received ${allTasks.size} tasks")
                    // Фильтруем по типу "daily"
                    allTasks.filter { it.type == "daily" }
                }
                Timber.d("getDailyTasks: filtered to ${tasks.size} daily tasks")
                dailyTasks.value = tasks
            } catch (e: Exception) {
                Timber.e(e, "getDailyTasks: error")
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
                    Timber.d("getMediumTasks: starting")
                    val allTasks = repository.getAllTasks()
                    Timber.d("getMediumTasks: received ${allTasks.size} tasks")
                    // Фильтруем по типу "medium"
                    allTasks.filter { it.type == "medium" }
                }
                Timber.d("getMediumTasks: filtered to ${tasks.size} medium tasks")
                mediumTasks.value = tasks
            } catch (e: Exception) {
                Timber.e(e, "getMediumTasks: error")
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
                    Timber.d("getLargeTasks: starting")
                    val allTasks = repository.getAllTasks()
                    Timber.d("getLargeTasks: received ${allTasks.size} tasks")
                    // Фильтруем по типу "large"
                    allTasks.filter { it.type == "large" }
                }
                Timber.d("getLargeTasks: filtered to ${tasks.size} large tasks")
                largeTasks.value = tasks
            } catch (e: Exception) {
                Timber.e(e, "getLargeTasks: error")
                errorString.value = e.message ?: "Ошибка загрузки крупных задач"
            } finally {
                loadProgress.value = false
            }
        }
    }
}