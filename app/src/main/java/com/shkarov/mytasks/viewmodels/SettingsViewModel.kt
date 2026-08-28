package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.domain.provider.ProviderKey
import com.shkarov.mytasks.network.AiProviderInterceptor
import com.shkarov.mytasks.network.BackendInterceptor
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themeSettings: ThemeSettings,
    private val providerInterceptor: AiProviderInterceptor,
    private val backendInterceptor: BackendInterceptor,
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

    val providerKeyFlow: StateFlow<String> = settingsStore.providerKeyFlow
        .onEach { key ->
            providerInterceptor.providerToken = key
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val backendUrlFlow: StateFlow<String> = settingsStore.backendUrlFlow
        .onEach { url ->
            backendInterceptor.baseUrl = url
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val backendApiKeyFlow: StateFlow<String> = settingsStore.backendApiKeyFlow
        .onEach { key ->
            backendInterceptor.apiKey = key
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val llmConnectionDirectType: StateFlow<Boolean> = settingsStore.llmDirectConnectionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true)

    val llmProvider: StateFlow<String> = settingsStore.llmProviderFlow
        .onEach { Timber.d("llmProvider - $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "")

    val llmModel: StateFlow<String> = settingsStore.llmModelFlow
        .onEach { Timber.d("llmModel - $it") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "")

    fun setLlmProvider(provider: AiProvider) {
        viewModelScope.launch {
            settingsStore.saveLlmProvider(provider.name)
            providerInterceptor.baseUrl = provider.host
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

    fun updateProviderKey(providerKey: ProviderKey) {
        viewModelScope.launch {
            settingsStore.saveProviderKey(providerKey)
            providerInterceptor.providerToken = providerKey.key
        }
    }

    fun setBackendUrl(url: String) {
        viewModelScope.launch {
            settingsStore.saveBackendUrl(url)
            backendInterceptor.baseUrl = url
        }
    }

    fun setBackendApiKey(key: String) {
        viewModelScope.launch {
            settingsStore.saveBackendApiKey(key)
            backendInterceptor.apiKey = key
        }
    }
}