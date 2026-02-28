package com.shkarov.mytasks.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun String.toEpochMillis(
    pattern: String = "yyyy-MM-dd HH:mm:ss",
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val ldt = LocalDateTime.parse(this, formatter)
    return ldt.atZone(zone).toInstant().toEpochMilli()
}