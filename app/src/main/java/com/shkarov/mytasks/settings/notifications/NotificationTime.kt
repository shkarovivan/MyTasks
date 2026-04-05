package com.shkarov.mytasks.settings.notifications

data class NotificationTime(
    val hour: Int = 9,
    val minute: Int = 0
) {
    fun formatted(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

