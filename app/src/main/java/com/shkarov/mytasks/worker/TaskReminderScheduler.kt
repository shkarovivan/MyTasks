package com.shkarov.mytasks.worker

import android.content.Context
import androidx.work.*
import com.shkarov.mytasks.data.speech.SpeechRecognitionImpl
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {

    private const val WORK_NAME = "daily_task_reminder"
    private const val NOTIFICATION_HOUR = 9
    private const val NOTIFICATION_MINUTE = 0

    fun schedule(context: Context) {
        Timber.w("schedule")
        val initialDelay = calculateDelayUntilNextTime(
            hour = NOTIFICATION_HOUR,
            minute = NOTIFICATION_MINUTE
        )

//        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(
//            repeatInterval = 15,
//            repeatIntervalTimeUnit = TimeUnit.MINUTES
//        )
//            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//            // Повторить через 30 минут если Worker вернул Result.retry()
//            .setBackoffCriteria(
//                BackoffPolicy.LINEAR,
//                30,
//                TimeUnit.MINUTES
//            )
//            // Никаких ограничений — работаем всегда
//            .setConstraints(Constraints.NONE)
//            .addTag(WORK_NAME)
//            .build()
//            .setInitialDelay(0, TimeUnit.SECONDS)     // без начальной задержки
//            .build()

//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            WORK_NAME,
//            ExistingPeriodicWorkPolicy.UPDATE,
//            request
//        )

            val request = OneTimeWorkRequestBuilder<TaskReminderWorker>().build()
            WorkManager.getInstance(context).enqueue(request)

    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Считает миллисекунды до следующего наступления указанного времени
     * Например сейчас 10:00, указали 09:00 → вернёт задержку до 09:00 следующего дня
     * Например сейчас 08:00, указали 09:00 → вернёт задержку до 09:00 сегодня
     */
    private fun calculateDelayUntilNextTime(hour: Int, minute: Int): Long {
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

        android.util.Log.d(
            "TaskReminderScheduler",
            "Следующее уведомление через: ${delay / 1000 / 60} минут"
        )

        return delay
    }
}