package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.settings.ThemeSettings
import com.shkarov.mytasks.settings.notifications.SettingsStore
import com.shkarov.mytasks.settings.notifications.NotificationTime
import com.shkarov.mytasks.worker.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themeSettings: ThemeSettings
) : AndroidViewModel(application) {

    private val settingsStore = SettingsStore(application)

    val lastTabRoute = settingsStore.lastTabRouteFlow

    fun saveLastTabRoute(route: String) {
        viewModelScope.launch {
            settingsStore.saveLastTabRoute(route)
        }
    }

    val notificationTime: StateFlow<NotificationTime> = settingsStore.notificationTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationTime())

    val notificationsEnabled: StateFlow<Boolean> = settingsStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkThemeEnabled = themeSettings.darkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { themeSettings.darkThemeFlow.first() }
        )

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeSettings.setDarkThemeEnabled(enabled)
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsStore.saveTime(hour, minute)
            TaskReminderScheduler.updateTime(getApplication(), hour, minute)
        }
    }

    fun onNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.saveEnabled(enabled)
            if (enabled) {
                TaskReminderScheduler.schedule(getApplication())
            } else {
                TaskReminderScheduler.cancel(getApplication())
            }
        }
    }
}