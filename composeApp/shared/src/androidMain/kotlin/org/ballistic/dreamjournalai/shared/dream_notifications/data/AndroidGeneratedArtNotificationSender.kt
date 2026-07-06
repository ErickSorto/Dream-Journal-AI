package org.ballistic.dreamjournalai.shared.dream_notifications.data

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ballistic.dreamjournalai.shared.DreamJournalAIApp
import org.ballistic.dreamjournalai.shared.R
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.GeneratedArtNotificationSender
import java.net.URL

class AndroidGeneratedArtNotificationSender(
    private val context: Context,
) : GeneratedArtNotificationSender {

    private val logger = Logger.withTag("GeneratedArtNotification")

    override suspend fun showDreamArtComplete(imageUrl: String, previewImageBytes: ByteArray?) {
        showNotification(
            notificationId = DREAM_ART_NOTIFICATION_ID,
            title = "Your dream art is ready",
            message = "Your generated dream image has been saved.",
            imageUrl = imageUrl,
            previewImageBytes = previewImageBytes
        )
    }

    override suspend fun showDreamWorldComplete(imageUrl: String, previewImageBytes: ByteArray?) {
        showNotification(
            notificationId = DREAM_WORLD_NOTIFICATION_ID,
            title = "Your dream world is ready",
            message = "Your new world painting has been saved.",
            imageUrl = imageUrl,
            previewImageBytes = previewImageBytes
        )
    }

    private suspend fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        imageUrl: String,
        previewImageBytes: ByteArray?,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            logger.w { "Skipping generated art notification because POST_NOTIFICATIONS is not granted." }
            return
        }

        val previewBitmap = loadPreviewBitmap(imageUrl, previewImageBytes)

        val builder = NotificationCompat.Builder(context, DreamJournalAIApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dream_journal_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_STATUS)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)

        createContentIntent(notificationId)?.let { pendingIntent ->
            builder.setContentIntent(pendingIntent)
        }

        if (previewBitmap != null) {
            builder
                .setLargeIcon(previewBitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(previewBitmap)
                        .setSummaryText(message)
                )
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    suspend fun showGeneratedArtPush(
        title: String,
        message: String,
        imageUrl: String,
        isWorld: Boolean,
    ) {
        showNotification(
            notificationId = if (isWorld) DREAM_WORLD_NOTIFICATION_ID else DREAM_ART_NOTIFICATION_ID,
            title = title,
            message = message,
            imageUrl = imageUrl,
            previewImageBytes = null
        )
    }

    private fun createContentIntent(notificationId: Int): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        return PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun loadPreviewBitmap(imageUrl: String, previewImageBytes: ByteArray?): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = previewImageBytes ?: when {
                    imageUrl.startsWith("data:image") -> {
                        Base64.decode(imageUrl.substringAfter("base64,"), Base64.DEFAULT)
                    }
                    imageUrl.isNotBlank() -> URL(imageUrl).readBytes()
                    else -> null
                }
                bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } catch (e: Exception) {
                logger.w(e) { "Unable to load generated art notification preview." }
                null
            }
        }

    private companion object {
        const val DREAM_ART_NOTIFICATION_ID = 9401
        const val DREAM_WORLD_NOTIFICATION_ID = 9402
    }
}
