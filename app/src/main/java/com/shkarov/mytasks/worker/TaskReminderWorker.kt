package com.shkarov.mytasks.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shkarov.mytasks.MainActivity
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TasksRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "task_reminder_channel_V2"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.w("createNotificationChannel")
            createNotificationChannel()

            val tasks = repository.getAllTasks()
            showNotification(tasks)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TaskReminderWorker", "Ошибка: ${e.message}")
            // Result.retry() — WorkManager повторит попытку через backoffDelay
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о задачах",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневные напоминания о задачах"
                // Уведомление не будет издавать звук повторно если уже показано
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(tasks: List<Task>) {
        Timber.w("showNotification")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskText = tasks.joinToString("\n") { it.title }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_work_24)
            .setContentTitle("📋 Задачи на сегодня (${tasks.size})")
            .setContentText(tasks.first().let { it.title })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskText)
                    .setBigContentTitle("📋 Задачи на сегодня (${tasks.size})")
                    .setSummaryText("Невыполненных: ${tasks.size}")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
        }
    }
}
