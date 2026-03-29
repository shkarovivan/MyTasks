package com.shkarov.mytasks.utils

import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


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
