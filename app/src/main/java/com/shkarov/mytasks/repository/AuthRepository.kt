package com.shkarov.mytasks.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow

data class AccountInfo(
    val userId: String,
    val email: String
)

// Google Sign-In via Credential Manager. The ID-token (~1h lifetime) is fetched
// on demand for backend requests; only account identity is persisted.
interface AuthRepository {
    val accountFlow: Flow<AccountInfo?>

    // Interactive sign-in: shows the Google account picker, must be called
    // from an Activity context (UI is required).
    suspend fun signIn(activityContext: Context): Result<AccountInfo>

    suspend fun signOut(activityContext: Context)

    // Returns a fresh ID-token for backend calls, or null when signed out /
    // no account is available anymore.
    suspend fun getIdToken(): String?

    // Drops the cached token so the next getIdToken() re-requests it
    // (used after a 401 from the backend).
    fun invalidateToken()
}
