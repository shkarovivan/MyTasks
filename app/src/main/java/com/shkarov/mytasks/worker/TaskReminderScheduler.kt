package com.shkarov.mytasks.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shkarov.mytasks.settings.notifications.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import timber.log.Timber
import java.util.Date
object TaskReminderScheduler {

    fun updateTime(context: Context, hour: Int, minute: Int) {
        Timber.d("Notification time updated to %02d:%02d", hour, minute)
        schedule(context, hour, minute)
    }

    fun schedule(context: Context, hour: Int? = null, minute: Int? = null) {
        Timber.d("TaskReminderScheduler: schedule")

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val prefs = SettingsStore(context)
            val notificationsEnabled = prefs.notificationsEnabledFlow.first()

            if (!notificationsEnabled) {
                Timber.d("TaskReminderScheduler: notifications disabled, skipping")
                return@launch
            }

            val scheduleHour = hour ?: prefs.notificationTimeFlow.first().hour
            val scheduleMinute = minute ?: prefs.notificationTimeFlow.first().minute

            doSchedule(context, scheduleHour, scheduleMinute)
        }
    }

    private fun doSchedule(context: Context, hour: Int, minute: Int) {
        Timber.d("TaskReminderScheduler: scheduling at %02d:%02d", hour, minute)

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmMgr.cancel(pendingIntent)

        val triggerTime = calculateNextTriggerTime(hour, minute)

        if (canScheduleExact(alarmMgr)) {
            Timber.d("TaskReminderScheduler: using setExactAndAllowWhileIdle")
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            Timber.d("TaskReminderScheduler: using setAndAllowWhileIdle")
            alarmMgr.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Timber.d("Alarm set for: $triggerTime (${Date(triggerTime)})")
    }

    fun cancel(context: Context) {
        Timber.d("TaskReminderScheduler: cancel")

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmMgr.cancel(pendingIntent)
        Timber.d("Notification cancelled")
    }

    private fun canScheduleExact(alarmMgr: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmMgr.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int): Long {
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
        Timber.d("Next alarm at %02d:%02d in ${delay / 1000 / 60} minutes", hour, minute)

        return target.timeInMillis
    }
}
