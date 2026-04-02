package com.shkarov.mytasks.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.model.DetailTaskUiState
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTaskViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailTaskUiState>(DetailTaskUiState.Loading)
    val uiState: StateFlow<DetailTaskUiState> = _uiState.asStateFlow()

    private var currentTaskId: String? = null

    fun loadTask(taskId: String, force: Boolean = false) {
        if (!force && currentTaskId == taskId && _uiState.value is DetailTaskUiState.Success) {
            return
        }

        currentTaskId = taskId

        viewModelScope.launch {
            _uiState.value = DetailTaskUiState.Loading

            runCatching {
                repository.getTaskById(taskId)
            }.onSuccess { task ->
                _uiState.value = DetailTaskUiState.Success(task)
            }.onFailure { error ->
                _uiState.value = DetailTaskUiState.Error(
                    error.message ?: "Не удалось загрузить задачу"
                )
            }
        }
    }
}
