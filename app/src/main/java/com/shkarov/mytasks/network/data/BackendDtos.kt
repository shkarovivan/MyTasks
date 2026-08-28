package com.shkarov.mytasks.network.data

import com.shkarov.mytasks.domain.model.Task

data class BackendTaskRequest(
    val text: String,
    val model: String = ""
)

data class BackendSearchRequest(
    val request: String,
    val tasks: List<Task>,
    val model: String = ""
)

data class BackendSearchResponse(
    val answer: String = "",
    val ids: List<String> = emptyList()
)
