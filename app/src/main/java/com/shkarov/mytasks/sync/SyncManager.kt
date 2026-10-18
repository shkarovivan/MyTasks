package com.shkarov.mytasks.sync

import com.shkarov.mytasks.data_base.TasksDbDao
import com.shkarov.mytasks.network.SyncApi
import com.shkarov.mytasks.network.data.SyncPushRequest
import com.shkarov.mytasks.repository.AuthRepository
import com.shkarov.mytasks.settings.AuthStore
import com.shkarov.mytasks.settings.SyncStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Sync engine: local Room is the offline cache, the backend Postgres is the
// source of truth. Conflicts resolve by last-write-wins on updatedAt (ms).
// All network work runs on a private IO scope; failures leave cursors intact
// so the next trigger retries the same delta.
@Singleton
class SyncManager @Inject constructor(
    private val syncApi: SyncApi,
    private val authRepository: AuthRepository,
    private val authStore: AuthStore,
    private val syncStore: SyncStore,
    private val tasksDao: TasksDbDao
) {
    private companion object {
        const val PUSH_DEBOUNCE_MS = 5_000L
        const val PERIODIC_SYNC_MS = 15 * 60_000L
        const val PAGE_SIZE = 500
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()
    private var debounceJob: Job? = null
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            authStore.userIdFlow.collect { userId ->
                if (userId != null) {
                    // Account bookkeeping only; the actual sync round needs a
                    // token and is driven by MainActivity.onStart (prefetch)
                    // and the triggers below.
                    onAccountChanged(userId)
                }
            }
        }
        scope.launch {
            while (true) {
                delay(PERIODIC_SYNC_MS)
                syncNow("periodic")
            }
        }
        Timber.d("✅ SyncManager started")
    }

    // Account lifecycle: first login backfills local tasks; switching to a
    // different account wipes the local cache (tasks belong to the account).
    // Blank ids are intermediate sign-in states (before whoami) and are
    // ignored — treating them as accounts caused spurious wipes.
    private suspend fun onAccountChanged(userId: String) {
        if (userId.isBlank()) return
        val stored = syncStore.userIdFlow.first()
        if (stored == userId) return

        if (stored == null) {
            tasksDao.stampUnsynced(System.currentTimeMillis())
            Timber.d("✅ first sync login, unsynced rows stamped")
        } else {
            tasksDao.deleteAllTasks()
            Timber.d("✅ sync account switched, local cache cleared")
        }
        syncStore.reset(userId)
        syncNow("account")
    }

    // Debounced after every local mutation.
    fun schedulePush() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(PUSH_DEBOUNCE_MS)
            syncNow("mutation")
        }
    }

    // One push + pull round; safe to call from anywhere.
    suspend fun syncNow(reason: String) {
        Timber.d("syncNow(%s) entered", reason)
        if (authStore.userIdFlow.first().isNullOrBlank()) {
            Timber.d("syncNow(%s): no user id", reason)
            return
        }
        if (authRepository.getIdToken() == null) {
            Timber.d("syncNow(%s): no token", reason)
            return
        }

        syncMutex.withLock {
            val okPush = push()
            val okPull = pull()
            Timber.d("sync(%s): push=%s pull=%s", reason, okPush, okPull)
        }
    }

    private suspend fun push(): Boolean {
        val since = syncStore.lastPushCursorFlow.first()
        val dirty = tasksDao.getTasksChangedSince(since)
        if (dirty.isEmpty()) return true

        for (chunk in dirty.chunked(PAGE_SIZE)) {
            val response = runCatching {
                syncApi.push(SyncPushRequest(chunk.map { it.toSyncTaskDto() }))
            }.onFailure {
                Timber.e(it, "❌ sync push failed")
                return false
            }.getOrThrow()

            if (!response.isSuccessful) {
                Timber.e("❌ sync push http %d", response.code())
                return false
            }
        }
        syncStore.saveCursors(
            pull = syncStore.lastPullCursorFlow.first(),
            push = dirty.maxOf { it.updatedAt }
        )
        Timber.d("✅ pushed %d tasks", dirty.size)
        return true
    }

    private suspend fun pull(): Boolean {
        var cursor = syncStore.lastPullCursorFlow.first()
        var applied = 0

        while (true) {
            val response = runCatching {
                syncApi.pull(since = cursor, limit = PAGE_SIZE)
            }.onFailure {
                Timber.e(it, "❌ sync pull failed")
                return false
            }.getOrThrow()

            if (!response.isSuccessful) {
                Timber.e("❌ sync pull http %d", response.code())
                return false
            }
            val page = response.body() ?: return false

            for (dto in page.tasks) {
                val local = tasksDao.getTaskByIdIncludingDeleted(dto.id)
                if (local == null || local.updatedAt < dto.updatedAt) {
                    tasksDao.insertTask(dto.toTask())
                    applied++
                }
            }
            cursor = page.cursor
            if (page.tasks.size < PAGE_SIZE) break
        }

        syncStore.saveCursors(
            pull = cursor,
            push = syncStore.lastPushCursorFlow.first()
        )
        if (applied > 0) Timber.d("✅ pulled %d tasks", applied)
        return true
    }
}
