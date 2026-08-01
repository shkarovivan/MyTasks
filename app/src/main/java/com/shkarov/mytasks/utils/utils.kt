package com.shkarov.mytasks.utils

import com.shkarov.mytasks.domain.model.Task
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * Orders tasks by how overdue they are: tasks with fewer overdue days
 * (not overdue yet, or the least overdue) come first; the most overdue
 * sink to the bottom. Tasks that are not past their deadline all tie at 0
 * and keep their previous relative order (stable sort).
 */
fun List<Task>.sortedByOverdueDays(now: Long = System.currentTimeMillis()): List<Task> =
    sortedBy { (now - it.deadLineMs).coerceAtLeast(0L) }


fun String.toEpochMillis(
    pattern: String = "yyyy-MM-dd HH:mm:ss",
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    try {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val ldt = LocalDateTime.parse(this, formatter)
        return ldt.atZone(zone).toInstant().toEpochMilli()
    } catch (e: Exception) {
        Timber.e("Ошибка парсинга даты: ${e.message}")
        return 0L
    }
}

fun getTomorrowTimestamp(): Long {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    return tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
