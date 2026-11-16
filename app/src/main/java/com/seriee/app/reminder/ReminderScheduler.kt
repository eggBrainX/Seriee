package com.seriee.app.reminder

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.debug.AppLogger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ReminderScheduler {
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure { err ->
            AppLogger.w("Reminder", "Could not open exact alarm settings", err)
        }
    }

    fun schedule(context: Context, item: MediaItem) {
        cancel(context, item.id)
        val release = item.releaseDate ?: return
        if (!item.reminderEnabled || release <= System.currentTimeMillis()) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID, item.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, item.title)
            putExtra(ReminderReceiver.EXTRA_TYPE, context.getString(item.type.labelRes))
        }
        val pi = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pi)
            }
        }.onFailure { err ->
            AppLogger.w("Reminder", "Exact alarm fallback for ${item.title}", err)
            runCatching { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, release, pi) }
                .onFailure { AppLogger.e("Reminder", "Failed to schedule alarm for ${item.title}", it) }
        }
    }

    fun cancel(context: Context, itemId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            itemId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }

    fun rescheduleAll(context: Context, items: List<MediaItem>) {
        items.forEach { schedule(context, it) }
    }

    fun triggerNow(context: Context, item: MediaItem) {
        ReminderReceiver.showNotification(
            context,
            item.id,
            context.getString(item.type.labelRes),
            item.title
        )
    }

    fun showDateTimePicker(context: Context, initialMs: Long?, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = initialMs ?: (System.currentTimeMillis() + 86_400_000L)
        }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, min)
                        cal.set(Calendar.SECOND, 0)
                        onPicked(cal.timeInMillis)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun formatReleaseDate(context: Context, ms: Long?): String {
        if (ms == null) return context.getString(R.string.date_not_set)
        val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return fmt.format(Date(ms))
    }

    fun countdownShortBadge(context: Context, ms: Long?): String {
        if (ms == null) return ""
        val diff = ms - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.badge_today)
        val days = (diff / 86_400_000L).toInt()
        if (days >= 1) return context.getString(R.string.badge_d_days, days)
        val hours = (diff / 3_600_000L).toInt()
        if (hours >= 1) return context.getString(R.string.badge_hours_left, hours)
        return context.getString(R.string.badge_soon)
    }

    fun countdownBadgeText(context: Context, ms: Long?): String {
        if (ms == null) return context.getString(R.string.no_schedule)
        val diff = ms - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.countdown_today)
        val days = (diff / 86_400_000L).toInt()
        return when {
            days > 1 -> context.getString(R.string.countdown_days, days)
            days == 1 -> context.getString(R.string.countdown_one_day)
            else -> {
                val hours = (diff / 3_600_000L).toInt()
                if (hours >= 1) context.getString(R.string.countdown_hours, hours)
                else context.getString(R.string.countdown_under_hour)
            }
        }
    }

    fun countdownDetailedText(context: Context, ms: Long?): String {
        if (ms == null) return context.getString(R.string.schedule_not_set)
        val diff = ms - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.released_today)
        val totalMin = (diff / 60_000L).toInt()
        val days = totalMin / (60 * 24)
        val hours = (totalMin % (60 * 24)) / 60
        val min = totalMin % 60
        return when {
            days > 0 -> if (hours > 0) context.getString(R.string.detailed_days_hours, days, hours)
            else context.getString(R.string.detailed_days, days)
            hours > 0 -> context.getString(R.string.detailed_hours_minutes, hours, min)
            else -> context.getString(R.string.detailed_minutes, maxOf(1, min))
        }
    }

    fun timeUntilRelease(context: Context, ms: Long?): String {
        if (ms == null) return context.getString(R.string.no_schedule)
        val diff = ms - System.currentTimeMillis()
        if (diff <= 0) return context.getString(R.string.already_aired)
        val days = (diff / 86_400_000L).toInt()
        val hours = ((diff % 86_400_000L) / 3_600_000L).toInt()
        val min = ((diff % 3_600_000L) / 60_000L).toInt()
        return when {
            days >= 1 -> context.getString(R.string.airs_in_days, days)
            hours >= 1 -> context.getString(R.string.airs_in_hours, hours)
            min >= 1 -> context.getString(R.string.airs_in_minutes, min)
            else -> context.getString(R.string.airs_soon)
        }
    }
}
