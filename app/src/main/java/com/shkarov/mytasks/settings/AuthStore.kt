package com.shkarov.mytasks.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

// Persisted Google account of the signed-in user. The ID-token itself is never
// stored: it lives ~1h and is re-requested on demand.
@Singleton
class AuthStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        val USER_ID = stringPreferencesKey("auth_user_id")
        val EMAIL = stringPreferencesKey("auth_email")
    }

    val emailFlow: Flow<String?> = context.authDataStore.data.map { it[EMAIL] }
    val userIdFlow: Flow<String?> = context.authDataStore.data.map { it[USER_ID] }

    suspend fun save(userId: String, email: String) {
        context.authDataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[EMAIL] = email
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { prefs ->
            prefs.remove(USER_ID)
            prefs.remove(EMAIL)
        }
    }
}
