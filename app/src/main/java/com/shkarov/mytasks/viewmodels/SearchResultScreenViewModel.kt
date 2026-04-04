package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.utils.getTomorrowTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultScreenViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private var ids = mutableListOf<String>()

    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun getTasks(ids: List<String>) {
        this.ids = ids.toMutableList()
        updateTasks()
    }

    fun getTodayTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            _tasks.value = repository.getTimedTasks(getTomorrowTimestamp())
        }
    }
    private fun updateTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            _tasks.value = ids
                .map { id ->
                    async { repository.getTaskById(id) }
                }
                .awaitAll()
        }
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskByID(taskId)
            ids.remove(taskId)
            updateTasks()
        }
    }

}