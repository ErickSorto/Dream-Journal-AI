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

class GenericReminderNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val title = inputData.getString(KEY_TITLE) ?: "DreamNorth reminder"
        val message = inputData.getString(KEY_MESSAGE) ?: "Open DreamNorth when you have a moment."
        val kind = inputData.getString(KEY_KIND) ?: KIND_DREAM_JOURNAL
        val destination = when (kind) {
            KIND_REALITY_CHECK -> NotificationDestination.RealityCheck
            KIND_PREMIUM_TRIAL -> NotificationDestination.Store
            else -> NotificationDestination.DreamJournal
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationNavigationController.EXTRA_DESTINATION, destination.rawValue)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val smallIconRes = when (kind) {
            KIND_REALITY_CHECK -> R.drawable.ic_reality_check_notification
            else -> R.drawable.ic_dream_journal_notification
        }
        val largeIconRes = when (kind) {
            KIND_REALITY_CHECK -> R.drawable.reality_check_notification_art
            else -> R.drawable.dream_journal_notification_art
        }
        val largeIcon = BitmapFactory.decodeResource(applicationContext.resources, largeIconRes)
        val notification = NotificationCompat.Builder(applicationContext, DreamJournalAIApp.CHANNEL_ID)
            .setSmallIcon(smallIconRes)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_KIND = "kind"
        const val KIND_DREAM_JOURNAL = "dream_journal"
        const val KIND_REALITY_CHECK = "reality_check"
        const val KIND_PREMIUM_TRIAL = "premium_trial"
    }
}
