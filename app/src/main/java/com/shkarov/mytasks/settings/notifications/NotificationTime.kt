package com.shkarov.mytasks.settings.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationTime(
    val hour: Int = 9,
    val minute: Int = 0
) {
    fun formatted(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

class NotificationTimeManager {
    private val _notificationTime = MutableStateFlow(NotificationTime())
    val notificationTime: StateFlow<NotificationTime> = _notificationTime.asStateFlow()

    fun updateTime(hour: Int, minute: Int) {
        _notificationTime.value = NotificationTime(hour, minute)
        // Здесь можно добавить логику: перепланировать уведомление,
        // сохранить в SharedPreferences, отправить на сервер и т.д.
        scheduleNotification(_notificationTime.value)
    }

    private fun scheduleNotification(time: NotificationTime) {
        // TODO: реализовать планирование уведомления через AlarmManager/WorkManager
        println("Уведомление запланировано на ${time.formatted()}")
    }
}

