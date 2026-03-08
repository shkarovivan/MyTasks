package com.shkarov.mytasks.worker

import android.content.Context
import androidx.work.*
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {

    private const val WORK_NAME = "daily_task_reminder"
    private const val NOTIFICATION_HOUR = 7
    private const val NOTIFICATION_MINUTE = 30

    fun schedule(context: Context) {
        Timber.w("schedule")
        val initialDelayMs = calculateDelayUntilNextTimeMs(
            hour = NOTIFICATION_HOUR,
            minute = NOTIFICATION_MINUTE
        )

        /**
         * For Production Notification
         */

        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.MINUTES
            )
            .addTag(WORK_NAME)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        /**
         * For Test Notification
         */
//            val request = OneTimeWorkRequestBuilder<TaskReminderWorker>().build()
//            WorkManager.getInstance(context).enqueue(request)

    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Считает миллисекунды до следующего наступления указанного времени
     * Например сейчас 10:00, указали 09:00 → вернёт задержку до 09:00 следующего дня
     * Например сейчас 08:00, указали 09:00 → вернёт задержку до 09:00 сегодня
     */
    private fun calculateDelayUntilNextTimeMs(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Если время уже прошло сегодня — переносим на завтра
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        Timber.d("Следующее уведомление через: ${delay / 1000 / 60} минут")

        return delay
    }
}