package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.utils.getTomorrowTimestamp
import com.shkarov.mytasks.utils.sortedByOverdueDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultScreenViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private var taskIds = mutableListOf<String>()

    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun getTasks(ids: List<String>) {
        taskIds = ids.toMutableList()
        collectAllTasks(taskIds)
    }

    fun getTodayTasks() {
        collectAllTasks(null)
    }

    fun collectAllTasks(ids: List<String>?) {
        viewModelScope.launch {
            repository.getTasksFlow().collect { tasks ->
                _tasks.value = tasks.filter { task ->
                    if (ids == null) {
                        task.deadLineMs < getTomorrowTimestamp()
                    } else {
                        task.id in taskIds
                    }
                }.let { filtered ->
                    // "Your tasks for today": sort by overdue days ascending —
                    // the least overdue (closest to their deadline) go to the top.
                    // Search results keep their relevance order.
                    if (ids == null) filtered.sortedByOverdueDays() else filtered
                }
            }
        }
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskIds.remove(taskId)
            repository.deleteTaskByID(taskId)
        }
    }

}