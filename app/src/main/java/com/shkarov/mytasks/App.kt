package com.shkarov.mytasks

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.shkarov.mytasks.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.shkarov.mytasks.worker.TaskReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ScheduleExactAlarm")
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        TaskReminderScheduler.schedule(this)
        syncManager.start()
    }
}