package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.data.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TasksRepository
): ViewModel() {
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