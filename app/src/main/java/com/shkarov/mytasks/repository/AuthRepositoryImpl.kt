package com.shkarov.mytasks.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.shkarov.mytasks.BuildConfig
import com.shkarov.mytasks.settings.AuthStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authStore: AuthStore
) : AuthRepository {

    private companion object {
        // Google ID-tokens live ~1h; refresh slightly earlier.
        private const val TOKEN_TTL_MS = 50 * 60 * 1000L
    }

    private val tokenMutex = Mutex()

    @Volatile
    private var cachedToken: String? = null
    private var cachedTokenAt = 0L

    override val accountFlow: Flow<AccountInfo?> =
        combine(authStore.userIdFlow, authStore.emailFlow) { userId, email ->
            if (email != null) AccountInfo(userId.orEmpty(), email) else null
        }

    override suspend fun signIn(activityContext: Context): Result<AccountInfo> {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            // userId (Google `sub`) is filled later from the backend /v1/auth/whoami.
            authStore.save(userId = "", email = credential.id)
            cachedToken = credential.idToken
            cachedTokenAt = System.currentTimeMillis()
            Timber.d("✅ signed in as ${credential.id}")
            Result.success(AccountInfo(userId = "", email = credential.id))
        } catch (e: GetCredentialCancellationException) {
            Timber.d("sign-in cancelled by user")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "❌ sign-in failed")
            Result.failure(e)
        }
    }

    override suspend fun signOut(activityContext: Context) {
        runCatching {
            CredentialManager.create(activityContext)
                .clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { Timber.e(it, "clearCredentialState failed") }
        authStore.clear()
        tokenMutex.withLock {
            cachedToken = null
            cachedTokenAt = 0L
        }
        Timber.d("✅ signed out")
    }

    override fun invalidateToken() {
        cachedToken = null
    }

    override suspend fun getIdToken(): String? {
        if (authStore.emailFlow.first() == null) return null

        tokenMutex.withLock {
            val token = cachedToken
            if (token != null &&
                System.currentTimeMillis() - cachedTokenAt < TOKEN_TTL_MS
            ) {
                return token
            }
        }

        // Silent refresh: only accounts that already consented are eligible,
        // so no UI is needed and the application context is enough.
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(appContext)
                .getCredential(appContext, request)
            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            tokenMutex.withLock {
                cachedToken = credential.idToken
                cachedTokenAt = System.currentTimeMillis()
            }
            credential.idToken
        } catch (e: NoCredentialException) {
            // The account vanished (removed from the device, credentials cleared):
            // drop the persisted account so the UI offers sign-in again.
            Timber.d("no google account available, resetting auth state")
            authStore.clear()
            tokenMutex.withLock {
                cachedToken = null
                cachedTokenAt = 0L
            }
            null
        } catch (e: Exception) {
            Timber.e(e, "❌ silent token refresh failed")
            null
        }
    }
}
