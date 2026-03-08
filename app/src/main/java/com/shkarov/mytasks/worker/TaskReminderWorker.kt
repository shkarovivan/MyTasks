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
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TasksRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.w("createNotificationChannel")
            createNotificationChannel()

            val tasks = repository.getTimedTasks(getTomorrowTimestamp())
            showNotification(tasks)

            Result.success()
        } catch (e: Exception) {
            Timber.e("Ошибка: ${e.message}")
            Result.retry()
        }
    }

    fun getTomorrowTimestamp(): Long {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NOTIFICATION_DESCRIPTION
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

        val taskText = tasks.joinToString("\n") { "- ${it.title}" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(context.getString(R.string.notification_title_text) + " (${tasks.size})")
            .setContentText(tasks.firstOrNull()?.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskText)
                    .setBigContentTitle(context.getString(R.string.notification_title_text) + " (${tasks.size})")
                    .setSummaryText(context.getString(R.string.notification_summary_text) + " ${tasks.size}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
        }
    }


    companion object {
        const val CHANNEL_ID = "task_reminder_channel_V3"
        const val NOTIFICATION_ID = 1001

        private const val NOTIFICATION_NAME = "tasks notifications"
        private const val NOTIFICATION_DESCRIPTION= "daily notifications about tasks"
    }

}
