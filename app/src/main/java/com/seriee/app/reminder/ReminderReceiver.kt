package com.seriee.app.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seriee.app.MainActivity
import com.seriee.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.default_series_title)
        val typeName = intent.getStringExtra(EXTRA_TYPE)
        val typeLabel = typeName?.let { name ->
            runCatching { com.seriee.app.data.MediaType.valueOf(name) }.getOrNull()?.let { context.getString(it.labelRes) }
        } ?: context.getString(R.string.type_series)

        ReminderScheduler.createNotificationChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_media_id", id)
        }
        val tapPending = PendingIntent.getActivity(
            context,
            id.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.airing_today_title, title))
            .setContentText(context.getString(R.string.airing_today_body, typeLabel, title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notification permission might be revoked
        }
    }

    companion object {
        const val EXTRA_ID = "extra_media_id"
        const val EXTRA_TITLE = "extra_media_title"
        const val EXTRA_TYPE = "extra_media_type"
    }
}
