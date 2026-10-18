package com.shkarov.mytasks.network.data

// Sync wire format; field names match the backend SyncTask JSON exactly
// (camelCase deadLine/deadLineMs, enums as "STARTED"/"WORK" name constants).
data class SyncTaskDto(
    val id: String,
    val created: String,
    val title: String,
    val description: String,
    val type: String,
    val deadLine: String,
    val deadLineMs: Long,
    val status: String,
    val work: String,
    val updatedAt: Long,
    val deleted: Boolean
)

data class SyncPushRequest(
    val tasks: List<SyncTaskDto>
)

data class SyncPushResponse(
    val applied: Int = 0
)

data class SyncPullResponse(
    val tasks: List<SyncTaskDto> = emptyList(),
    val cursor: Long = 0
)
