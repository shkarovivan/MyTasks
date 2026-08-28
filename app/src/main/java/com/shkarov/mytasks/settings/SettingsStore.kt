package com.shkarov.mytasks.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shkarov.mytasks.BuildConfig
import com.shkarov.mytasks.domain.provider.ProviderKey
import com.shkarov.mytasks.screens.Screens
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
){

    private companion object {

        private const val TAG = "SettingsStore"
        val KEY_HOUR = intPreferencesKey("notification_hour")
        val KEY_MINUTE = intPreferencesKey("notification_minute")
        val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LAST_TAB_ROUTE = stringPreferencesKey("last_tab_route")
        val LLM_DIRECT_CONNECTION = booleanPreferencesKey("llm_direct_connection")

        val LLM_PROVIDER = stringPreferencesKey("llm_provider")

        val LLM_MODEL = stringPreferencesKey("llm_model")

        val CURRENT_PROVIDER_KEY = stringPreferencesKey("current_provider_key")
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val BACKEND_API_KEY = stringPreferencesKey("backend_api_key")
        const val DEFAULT_HOUR = 7
        const val DEFAULT_MINUTE = 30
    }

    val lastTabRouteFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_TAB_ROUTE] ?: Screens.WorkTasks.route
    }

    val llmDirectConnectionFlow : Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LLM_DIRECT_CONNECTION] ?: true
    }

    val llmProviderFlow : Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LLM_PROVIDER] ?: ""
    }

    val llmModelFlow : Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LLM_MODEL] ?: ""
    }

    suspend fun saveLastTabRoute(route: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TAB_ROUTE] = route
        }
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ENABLED] ?: false
    }

    val notificationTimeFlow: Flow<NotificationTime> = context.dataStore.data.map { prefs ->
        NotificationTime(
            hour = prefs[KEY_HOUR] ?: DEFAULT_HOUR,
            minute = prefs[KEY_MINUTE] ?: DEFAULT_MINUTE
        )
    }

    val providerKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_PROVIDER_KEY].orEmpty()
    }

    val backendUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BACKEND_URL] ?: BuildConfig.BACKEND_URL
    }

    val backendApiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BACKEND_API_KEY] ?: BuildConfig.BACKEND_API_KEY
    }

    suspend fun saveEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = enabled
        }
    }

    suspend fun saveTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOUR] = hour
            prefs[KEY_MINUTE] = minute
        }
    }

    suspend fun saveLlmConnectionDirectType(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LLM_DIRECT_CONNECTION] = enabled
        }
    }

    suspend fun saveLlmProvider(provider:  String) {
        context.dataStore.edit { prefs ->
            prefs[LLM_PROVIDER] = provider
            prefs[CURRENT_PROVIDER_KEY] = prefs[stringPreferencesKey(provider)].orEmpty()
        }
    }

    suspend fun saveLlmModel(model:  String) {
        context.dataStore.edit { prefs ->
            prefs[LLM_MODEL] = model
        }
    }

    suspend fun saveProviderKey(providerKey: ProviderKey) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(providerKey.providerName)] = providerKey.key
            prefs[CURRENT_PROVIDER_KEY] = providerKey.key
        }
    }

    suspend fun saveBackendUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_URL] = url
        }
    }

    suspend fun saveBackendApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_API_KEY] = key
        }
    }
}

