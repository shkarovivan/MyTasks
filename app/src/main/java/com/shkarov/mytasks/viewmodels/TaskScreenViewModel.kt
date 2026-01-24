package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
class TaskScreenViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {
    var errorString = MutableStateFlow<String?>(null)
    var loadProgress = MutableStateFlow(false)
    private val _dailyTasks = MutableStateFlow<List<Task>>(emptyList())
    val dailyTasks: StateFlow<List<Task>> = _dailyTasks.asStateFlow()
    private val _mediumTasks = MutableStateFlow<List<Task>>(emptyList())
    val mediumTasks = _mediumTasks.asStateFlow()
    private val _largeTasks = MutableStateFlow<List<Task>>(emptyList())
    val largeTasks = _largeTasks.asStateFlow()

    init {
        collectAllTasks()
    }

    fun collectAllTasks() {
        viewModelScope.launch {
            repository.getTasksFlow().collect { tasks ->
                Timber.d("TaskScreenViewModel tasks - $tasks")

                _dailyTasks.value = tasks.filter { task ->
                    task.type == Type.DAILY.value
                }

                _mediumTasks.value = tasks.filter { task ->
                    task.type == Type.MEDIUM.value
                }

                _largeTasks.value = tasks.filter { task ->
                    task.type == Type.LARGE.value
                }
            }
        }
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch {
            repository.deleteTaskByID(taskId)
        }
    }
}