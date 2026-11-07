package com.seriee.app.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seriee.app.MainActivity
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import java.text.SimpleDateFormat
import java.util.Locale

object ReminderScheduler {
    const val CHANNEL_ID = "seriee_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, item: MediaItem) {
        val release = item.releaseDate ?: return
        if (release <= System.currentTimeMillis() || !item.reminderEnabled) return

        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID, item.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, item.title)
            putExtra(ReminderReceiver.EXTRA_TYPE, item.type.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, release, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, itemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun sendImmediateNotification(context: Context, title: String, message: String, itemId: String) {
        createNotificationChannel(context)
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_media_id", itemId)
        }
        val tapPending = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_title_prefix, title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(itemId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Ignored if permission revoked
        }
    }

    fun formatReleaseDate(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return context.getString(R.string.date_not_set)
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm", Locale.getDefault())
        return sdf.format(ts)
    }

    fun formatShortDate(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(ts)
    }

    fun timeUntilRelease(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return context.getString(R.string.no_schedule)
        val diff = ts - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.already_aired)
        val minutes = diff / (60 * 1000)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> context.getString(R.string.airs_in_days, days)
            hours > 0 -> context.getString(R.string.airs_in_hours, hours)
            minutes > 0 -> context.getString(R.string.airs_in_minutes, minutes)
            else -> context.getString(R.string.airs_soon)
        }
    }

    fun daysUntilRelease(ts: Long?): Int {
        if (ts == null || ts <= 0) return 0
        val diff = ts - System.currentTimeMillis()
        if (diff <= 0) return 0
        return ((diff + 86400000L - 1) / 86400000L).toInt()
    }

    fun countdownShortBadge(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return context.getString(R.string.badge_soon)
        val diff = ts - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.badge_today)
        val days = diff / (86400 * 1000)
        return when {
            days > 0 -> context.getString(R.string.badge_d_days, days)
            else -> {
                val hours = (diff / (3600 * 1000)).coerceAtLeast(1)
                context.getString(R.string.badge_hours_left, hours)
            }
        }
    }

    fun countdownBadgeText(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return context.getString(R.string.status_coming_soon)
        val diff = ts - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.countdown_today)
        val days = diff / (86400 * 1000)
        val hours = (diff / (3600 * 1000))
        return when {
            days > 1 -> context.getString(R.string.countdown_days, days)
            days == 1L -> context.getString(R.string.countdown_one_day)
            hours > 0 -> context.getString(R.string.countdown_hours, hours)
            else -> context.getString(R.string.countdown_under_hour)
        }
    }

    fun countdownDetailedText(context: Context, ts: Long?): String {
        if (ts == null || ts <= 0) return context.getString(R.string.schedule_not_set)
        val diff = ts - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.released_today)
        val totalHours = diff / (3600 * 1000)
        val days = diff / (86400 * 1000)
        val remainingHours = totalHours % 24
        val minutes = (diff / (60 * 1000)) % 60

        return when {
            days > 0 -> if (remainingHours > 0) context.getString(R.string.detailed_days_hours, days, remainingHours)
            else context.getString(R.string.detailed_days, days)
            totalHours > 0 -> context.getString(R.string.detailed_hours_minutes, totalHours, minutes)
            else -> context.getString(R.string.detailed_minutes, minutes)
        }
    }

    fun showDateTimePicker(
        context: Context,
        initialDate: Long? = null,
        onDateTimeSelected: (Long) -> Unit
    ) {
        val cal = java.util.Calendar.getInstance()
        if (initialDate != null && initialDate > 0) {
            cal.timeInMillis = initialDate
        } else {
            cal.add(java.util.Calendar.DAY_OF_YEAR, 3)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 19)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
        }

        val datePicker = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(java.util.Calendar.YEAR, year)
                cal.set(java.util.Calendar.MONTH, month)
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker = android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(java.util.Calendar.MINUTE, minute)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        onDateTimeSelected(cal.timeInMillis)
                    },
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE),
                    true
                )
                timePicker.setTitle(context.getString(R.string.pick_airing_time))
                timePicker.show()
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePicker.setTitle(context.getString(R.string.pick_release_date_title))
        datePicker.show()
    }
}
