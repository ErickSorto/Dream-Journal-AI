package org.ballistic.dreamjournalai.shared.dream_notifications.data

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.ballistic.dreamjournalai.shared.DreamJournalAIApp
import org.ballistic.dreamjournalai.shared.MainActivity
import org.ballistic.dreamjournalai.shared.R
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController

class DailyTokenReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val scheduler = AndroidDailyTokenReminderScheduler(applicationContext)

        if (scheduler.shouldShowReminderToday()) {
            showNotification()
        }

        scheduler.scheduleNext()
        return Result.success()
    }

    private fun showNotification() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(
                NotificationNavigationController.EXTRA_DESTINATION,
                NotificationDestination.DailyTokens.rawValue
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = inputData.getString(KEY_TITLE) ?: "Claim your daily token"
        val message = inputData.getString(KEY_MESSAGE) ?: "Your DreamNorth token is ready."
        val largeIcon = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.daily_token_notification_art
        )
        val notification = NotificationCompat.Builder(applicationContext, DreamJournalAIApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dream_token_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        private const val NOTIFICATION_ID = 8108
    }
}
