package org.ballistic.dreamjournalai.shared.dream_notifications.data

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UserNotifications.UNNotificationAttachment

private const val AttachmentTypeHintKey = "UNNotificationAttachmentOptionsTypeHintKey"
private const val JpegTypeIdentifier = "public.jpeg"
private const val PngTypeIdentifier = "public.png"

private data class NotificationImageResource(
    val name: String,
    val extension: String,
    val typeIdentifier: String
)

internal fun notificationImageAttachment(name: String): UNNotificationAttachment? {
    val artFallbackName = name
        .takeIf { it.endsWith("_attachment") }
        ?.removeSuffix("_attachment")
        ?.plus("_art")
    val candidates = listOfNotNull(
        NotificationImageResource(name, "jpg", JpegTypeIdentifier),
        NotificationImageResource(name, "png", PngTypeIdentifier),
        artFallbackName?.let { NotificationImageResource(it, "jpg", JpegTypeIdentifier) },
        artFallbackName?.let { NotificationImageResource(it, "png", PngTypeIdentifier) }
    )
    val resolved = candidates.firstNotNullOfOrNull { candidate ->
        NSBundle.mainBundle.pathForResource(
            name = candidate.name,
            ofType = candidate.extension
        )?.let { path -> candidate to path }
    } ?: return null
    val (resource, path) = resolved
    val url = NSURL.fileURLWithPath(path)
    return UNNotificationAttachment.attachmentWithIdentifier(
        identifier = name,
        URL = url,
        options = mapOf(
            AttachmentTypeHintKey to resource.typeIdentifier
        ),
        error = null
    )
}
