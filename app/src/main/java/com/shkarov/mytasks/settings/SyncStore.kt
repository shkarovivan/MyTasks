package com.shkarov.mytasks.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

// Sync bookkeeping: pull/push cursors (updatedAt ms of the last seen row) and
// the user_id the local database currently belongs to.
@Singleton
class SyncStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        val LAST_PULL_CURSOR = longPreferencesKey("last_pull_cursor")
        val LAST_PUSH_CURSOR = longPreferencesKey("last_push_cursor")
        val USER_ID = stringPreferencesKey("sync_user_id")
    }

    val lastPullCursorFlow: Flow<Long> = context.syncDataStore.data.map { it[LAST_PULL_CURSOR] ?: 0L }
    val lastPushCursorFlow: Flow<Long> = context.syncDataStore.data.map { it[LAST_PUSH_CURSOR] ?: 0L }
    val userIdFlow: Flow<String?> = context.syncDataStore.data.map { it[USER_ID] }

    suspend fun saveCursors(pull: Long, push: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_PULL_CURSOR] = pull
            prefs[LAST_PUSH_CURSOR] = push
        }
    }

    suspend fun saveUserId(userId: String) {
        context.syncDataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    suspend fun reset(userId: String) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_PULL_CURSOR] = 0L
            prefs[LAST_PUSH_CURSOR] = 0L
            prefs[USER_ID] = userId
        }
    }
}
