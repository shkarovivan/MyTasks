package com.shkarov.mytasks.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            Timber.d("$TAG doWork")

            createNotificationChannel()

            val tasks = repository.getTimedTasks(getTomorrowTimestamp())

            if (tasks.isNotEmpty() && canPostNotifications()) {
                showNotification(tasks)
            } else {
                Timber.d(
                    "$TAG notification skipped: tasks=${tasks.size}, canPost=${canPostNotifications()}"
                )
            }

            TaskReminderScheduler.reschedule(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error during reminder work")
            Result.retry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTomorrowTimestamp(): Long {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

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

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(tasks: List<Task>) {
        Timber.d("$TAG showNotification")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskText = tasks.joinToString("\n") { "- ${it.title}" }
        val title = "${context.getString(R.string.notification_title_text)} (${tasks.size})"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentTitle(context.getString(R.string.notification_title_text) + " (${tasks.size})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskText)
                    .setBigContentTitle(title)
                    .setSummaryText(
                        "${context.getString(R.string.notification_summary_text)} ${tasks.size}"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        private const val TAG = "TaskReminderWorker"
        private const val CHANNEL_ID = "task_reminder_channel_V4"
        private const val NOTIFICATION_ID = 1001

        private const val NOTIFICATION_NAME = "tasks notifications"
        private const val NOTIFICATION_DESCRIPTION= "daily notifications about tasks"
    }
}
