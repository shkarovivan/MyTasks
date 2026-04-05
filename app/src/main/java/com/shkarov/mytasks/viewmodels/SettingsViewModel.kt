package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.network.DynamicUrlInterceptor
import com.shkarov.mytasks.repository.AiProvider
import com.shkarov.mytasks.repository.AiProvidersRepository
import com.shkarov.mytasks.settings.ThemeSettings
import com.shkarov.mytasks.settings.SettingsStore
import com.shkarov.mytasks.settings.NotificationTime
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
    private val themeSettings: ThemeSettings,
    private val dynamicUrlInterceptor: DynamicUrlInterceptor,
    aiProviderRepository: AiProvidersRepository
) : AndroidViewModel(application) {

    private val settingsStore = SettingsStore(application)

    val lastTabRoute = settingsStore.lastTabRouteFlow

    val aiProviders = aiProviderRepository.getAiProviders()

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

    val llmConnectionDirectType: StateFlow<Boolean> = settingsStore.llmDirectConnectionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true)

    val llmProvider: StateFlow<String> = settingsStore.llmProviderFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "")

    val llmModel: StateFlow<String> = settingsStore.llmModelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "")

    fun setLlmProvider(provider: AiProvider) {
        viewModelScope.launch {
            settingsStore.saveLlmProvider(provider.name)
            dynamicUrlInterceptor.baseUrl = provider.host
        }
    }

    fun setLlmModel(model: String) {
        viewModelScope.launch {
            settingsStore.saveLlmModel(model)
        }
    }

    fun setConnectionDirectType(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.saveLlmConnectionDirectType(enabled = enabled)
        }
    }

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