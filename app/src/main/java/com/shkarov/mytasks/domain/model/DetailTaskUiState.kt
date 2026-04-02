package com.shkarov.mytasks.domain.model

sealed interface DetailTaskUiState {
    data object Loading : DetailTaskUiState
    data class Success(val task: Task) : DetailTaskUiState
    data class Error(val message: String) : DetailTaskUiState
}
