package org.ballistic.dreamjournalai.shared.dream_debug.data

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
import org.ballistic.dreamjournalai.shared.DreamJournalAIApp
import org.ballistic.dreamjournalai.shared.MainActivity
import org.ballistic.dreamjournalai.shared.R
import org.ballistic.dreamjournalai.shared.dream_debug.domain.DebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController

class AndroidDebugNotificationTester(
    private val context: Context,
) : DebugNotificationTester {
    override suspend fun showDreamTokenNotification() {
        showNotification(
            notificationId = 9301,
            title = "Claim your daily token",
            message = "Your DreamNorth token is ready.",
            smallIcon = R.drawable.ic_dream_token_notification,
            largeIcon = R.drawable.daily_token_notification_art,
            destination = NotificationDestination.DailyTokens
        )
    }

    override suspend fun showDreamJournalNotification() {
        showNotification(
            notificationId = 9302,
            title = "Write in your dream journal",
            message = "Take a minute to save what you remember.",
            smallIcon = R.drawable.ic_dream_journal_notification,
            largeIcon = R.drawable.dream_journal_notification_art,
            destination = NotificationDestination.DreamJournal
        )
    }

    override suspend fun showRealityCheckNotification() {
        showNotification(
            notificationId = 9303,
            title = "Reality check",
            message = "Pause and ask: am I dreaming?",
            smallIcon = R.drawable.ic_reality_check_notification,
            largeIcon = R.drawable.reality_check_notification_art,
            destination = NotificationDestination.RealityCheck
        )
    }

    private fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        smallIcon: Int,
        largeIcon: Int,
        destination: NotificationDestination,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw IllegalStateException("Notification permission is not granted")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationNavigationController.EXTRA_DESTINATION, destination.rawValue)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val artwork = BitmapFactory.decodeResource(context.resources, largeIcon)
        val notification = NotificationCompat.Builder(context, DreamJournalAIApp.CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setLargeIcon(artwork)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
