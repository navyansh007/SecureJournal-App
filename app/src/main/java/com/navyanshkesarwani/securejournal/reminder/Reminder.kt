package com.navyanshkesarwani.securejournal.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.navyanshkesarwani.securejournal.MainActivity
import com.navyanshkesarwani.securejournal.R
import com.navyanshkesarwani.securejournal.data.SettingsStore
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** WorkManager runs this once a day at the reminder time. */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Reminder.showNotification(applicationContext)
        return Result.success()
    }
}

object Reminder {

    private const val CHANNEL_ID = "daily_reminder"
    private const val WORK_NAME = "daily_reminder"
    private const val NOTIFICATION_ID = 1

    /** Schedules (or cancels) the daily reminder to match the current settings. */
    fun update(context: Context) {
        val workManager = WorkManager.getInstance(context)
        if (!SettingsStore.reminderEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextReminder(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    private fun millisUntilNextReminder(): Long {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(SettingsStore.reminderTime)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    /** Android 13+ needs the user's permission before an app can show notifications. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun showNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Daily reminder", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // Tapping the notification opens the app (which shows the lock screen first).
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to journal")
            .setContentText("How was your day? Take a few minutes to write it down.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
