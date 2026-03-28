package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.settings.ThemeSettings
import com.shkarov.mytasks.settings.notifications.NotificationPreferences
import com.shkarov.mytasks.settings.notifications.NotificationTime
import com.shkarov.mytasks.worker.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themeSettings: ThemeSettings
) : AndroidViewModel(application) {

    private val notificationPrefs = NotificationPreferences(application)

    val notificationTime: StateFlow<NotificationTime> = notificationPrefs.notificationTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationTime())

    val notificationsEnabled: StateFlow<Boolean> = notificationPrefs.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkThemeEnabled = themeSettings.darkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeSettings.setDarkThemeEnabled(enabled)
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationPrefs.saveTime(hour, minute)
            TaskReminderScheduler.updateTime(getApplication(), hour, minute)
        }
    }

    fun onNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPrefs.saveEnabled(enabled)
            if (enabled) {
                TaskReminderScheduler.schedule(getApplication())
            } else {
                TaskReminderScheduler.cancel(getApplication())
            }
        }
    }
}