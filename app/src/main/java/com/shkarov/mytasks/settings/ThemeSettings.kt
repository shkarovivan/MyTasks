package com.shkarov.mytasks.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "app_settings")

class ThemeSettings @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val darkThemeKey = booleanPreferencesKey("dark_theme_enabled")

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[darkThemeKey] ?: false
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[darkThemeKey] = enabled
        }
    }
}