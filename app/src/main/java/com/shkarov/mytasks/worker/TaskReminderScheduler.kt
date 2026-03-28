package com.shkarov.mytasks.worker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.util.Calendar
import timber.log.Timber
import java.util.Date

object TaskReminderScheduler {

    private const val NOTIFICATION_HOUR = 7
    private const val NOTIFICATION_MINUTE = 30

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(context: Context) {
        Timber.d("TaskReminderScheduler: schedule via AlarmManager")

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.cancel(pendingIntent)

        val triggerTime = calculateNextTriggerTime()

        if (!alarmMgr.canScheduleExactAlarms()) {
            Timber.d("TaskReminderScheduler: schedule via setInexactRepeating")
            alarmMgr.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            Timber.d("TaskReminderScheduler: schedule via setExact")
            alarmMgr.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Timber.d("Alarm set for: $triggerTime (${Date(triggerTime)})")
    }

    private fun calculateNextTriggerTime(): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
            set(Calendar.MINUTE, NOTIFICATION_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis
        Timber.d("Next alarm in ${delay / 1000 / 60} minutes")

        return target.timeInMillis
    }
}
