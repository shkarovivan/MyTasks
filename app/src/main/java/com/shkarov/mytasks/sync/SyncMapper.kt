package com.shkarov.mytasks.sync

import com.shkarov.mytasks.domain.model.StatusConverter
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.WorkConverter
import com.shkarov.mytasks.network.data.SyncTaskDto

// Task <-> SyncTaskDto mapping. Enum values travel as name() constants
// ("STARTED", "WORK") matching both Gson serialization and the backend wire
// format; the existing Room converters provide the tolerant reverse parse.
private val statusConverter = StatusConverter()
private val workConverter = WorkConverter()

fun Task.toSyncTaskDto(): SyncTaskDto = SyncTaskDto(
    id = id,
    created = created,
    title = title,
    description = description,
    type = type,
    deadLine = deadLine,
    deadLineMs = deadLineMs,
    status = status.name,
    work = work.name,
    updatedAt = updatedAt,
    deleted = deleted
)

fun SyncTaskDto.toTask(): Task = Task(
    id = id,
    created = created,
    title = title,
    description = description,
    type = type,
    deadLine = deadLine,
    deadLineMs = deadLineMs,
    status = statusConverter.convertStringToStatus(status),
    work = workConverter.convertStringToWork(work),
    updatedAt = updatedAt,
    deleted = deleted
)
