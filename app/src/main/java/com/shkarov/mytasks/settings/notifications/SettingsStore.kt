package com.shkarov.mytasks.settings.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shkarov.mytasks.screens.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

class SettingsStore(private val context: Context) {

    private companion object {
        val KEY_HOUR = intPreferencesKey("notification_hour")
        val KEY_MINUTE = intPreferencesKey("notification_minute")
        val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
        const val DEFAULT_HOUR = 7
        const val DEFAULT_MINUTE = 30
    }

    private val LAST_TAB_ROUTE = stringPreferencesKey("last_tab_route")

    val lastTabRouteFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_TAB_ROUTE] ?: Screens.WorkTasks.route
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
}

