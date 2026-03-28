package com.shkarov.mytasks.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.shkarov.mytasks.MainActivity
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class TaskReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TasksRepository
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("TaskReminderReceiver: onReceive")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission(context)) {
            Timber.w("Notification permission not granted")
            return
        }

        scope.launch {
            try {
                val tasks = repository.getTimedTasks(getTomorrowTimestamp())
                showNotification(context, tasks)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching tasks")
            } finally {
                // Перепланируем на следующий день
                TaskReminderScheduler.schedule(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTomorrowTimestamp(): Long {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, tasks: List<Task>) {
        Timber.d("showNotification: ${tasks.size} tasks")

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
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskText)
                    .setBigContentTitle(title)
                    .setSummaryText("${context.getString(R.string.notification_summary_text)} ${tasks.size}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val CHANNEL_ID = "task_reminder_channel_V4"
        private const val NOTIFICATION_ID = 1001
    }
}
