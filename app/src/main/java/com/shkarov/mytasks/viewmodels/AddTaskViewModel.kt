package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TasksRepository
): ViewModel() {
    /**
     * Task being edited. `null` in create mode or until the task finishes loading.
     * Inserting a [Task] whose id already exists performs an upsert (REPLACE),
     * so the same [addTask] path handles both create and edit.
     */
    private val _taskToEdit = MutableStateFlow<Task?>(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit.asStateFlow()

    fun loadTaskForEdit(taskId: String) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repository.getTaskById(taskId) }
            }.onSuccess { task ->
                _taskToEdit.value = task
            }.onFailure { error ->
                Timber.e("Ошибка загрузки задачи для редактирования: ${error.message}")
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertTask(task)
                }
                Timber.i("Задача успешно сохранена: ${task.title}")
            } catch (e: Exception) {
                Timber.e("Ошибка при сохранении задачи: ${e.message}")
            }
        }
    }
}
