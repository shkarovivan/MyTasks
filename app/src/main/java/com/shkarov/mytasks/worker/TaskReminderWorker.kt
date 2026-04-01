package com.shkarov.mytasks.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.shkarov.mytasks.MainActivity
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import com.shkarov.mytasks.utils.getTomorrowTimestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TaskReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TasksRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("TaskReminderReceiver: onReceive")

        val appContext = context.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission(appContext)
        ) {
            Timber.w("Notification permission not granted")
            TaskReminderScheduler.schedule(appContext)
            return
        }

        val pendingResult = goAsync()

        scope.launch {
            try {
                val tasks = repository.getTimedTasks(getTomorrowTimestamp())
                showNotification(appContext, tasks)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching tasks")
            } finally {
                TaskReminderScheduler.schedule(appContext)
                pendingResult.finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, tasks: List<Task>) {
        Timber.d("showNotification: ${tasks.size} tasks")

        ensureNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NAVIGATE_TO, NAVIGATE_TO_ROUTE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskText = tasks.joinToString("\n") { "- ${it.title}" }
        val title = "${context.getString(R.string.notification_title_text)} (${tasks.size})"

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon_notify
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notify_small)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(taskText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskText)
                    .setBigContentTitle(title)
//                    .setSummaryText("(${tasks.size})")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        Timber.d("Notification posted, id=$NOTIFICATION_ID, channel=$CHANNEL_ID")
    }

    private fun ensureNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily reminders about your tasks"
        }

        notificationManager.createNotificationChannel(channel)
        Timber.d("Notification channel ensured: $CHANNEL_ID")
    }

    private fun hasNotificationPermission(context: Context): Boolean {
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
        private const val CHANNEL_ID = "task_reminder_channel_V4"
        private const val NOTIFICATION_ID = 1001

        const val NAVIGATE_TO = "navigate_to"
        const val NAVIGATE_TO_ROUTE = "today_tasks"
    }
}
