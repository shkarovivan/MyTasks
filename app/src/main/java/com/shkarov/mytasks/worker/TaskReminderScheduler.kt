package com.shkarov.mytasks.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {

    private const val WORK_NAME = "daily_task_reminder"
    private const val NOTIFICATION_HOUR = 7
    private const val NOTIFICATION_MINUTE = 30

    fun schedule(context: Context) {
        Timber.d("TaskReminderScheduler: schedule")

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                buildRequest()
            )
    }

    fun reschedule(context: Context) {
        Timber.d("TaskReminderScheduler: reschedule")

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                buildRequest()
            )
    }

    fun cancel(context: Context) {
        Timber.d("TaskReminderScheduler: cancel")

        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(WORK_NAME)
    }

    private fun buildRequest(): OneTimeWorkRequest {
        val initialDelayMs = calculateDelayUntilNextTimeMs(
            hour = NOTIFICATION_HOUR,
            minute = NOTIFICATION_MINUTE
        )

        Timber.d("TaskReminderScheduler: initialDelayMs=$initialDelayMs")

        return OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1,
                TimeUnit.MINUTES
            )
            .addTag(WORK_NAME)
            .build()
    }

    /**
     * Считает миллисекунды до следующего наступления указанного времени.
     * Например:
     * - сейчас 10:00, указали 09:00 -> вернет задержку до 09:00 следующего дня
     * - сейчас 08:00, указали 09:00 -> вернет задержку до 09:00 сегодня
     */
    private fun calculateDelayUntilNextTimeMs(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis
        Timber.d("Следующее уведомление через ${delay / 1000 / 60} минут")

        return delay
    }
}
