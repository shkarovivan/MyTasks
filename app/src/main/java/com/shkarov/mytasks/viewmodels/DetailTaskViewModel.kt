package com.shkarov.mytasks.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.DetailTaskUiState
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTaskViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailTaskUiState>(DetailTaskUiState.Loading)
    val uiState: StateFlow<DetailTaskUiState> = _uiState.asStateFlow()

    private var currentTaskId: String? = null
    private var observeJob: Job? = null

    fun loadTask(taskId: String, force: Boolean = false) {
        if (!force && currentTaskId == taskId && _uiState.value is DetailTaskUiState.Success) {
            return
        }

        currentTaskId = taskId
        observeJob?.cancel()
        _uiState.value = DetailTaskUiState.Loading

        // Observe the task reactively: when it is edited (insertTask updates the row)
        // the Flow re-emits and the details screen refreshes automatically — no need
        // to leave and re-enter the screen.
        observeJob = viewModelScope.launch {
            repository.getTaskByIdFlow(taskId)
                .catch { error ->
                    _uiState.value = DetailTaskUiState.Error(
                        error.message ?: "Не удалось загрузить задачу"
                    )
                }
                .collect { task ->
                    _uiState.value = if (task != null) {
                        DetailTaskUiState.Success(task)
                    } else {
                        DetailTaskUiState.Error("Задача не найдена")
                    }
                }
        }
    }
}
