package org.ballistic.dreamjournalai.shared.dream_notifications.data

import io.ktor.util.decodeBase64Bytes
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.ballistic.dreamjournalai.shared.core.util.downloadImageBytes
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.GeneratedArtNotificationSender
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IosGeneratedArtNotificationSender : GeneratedArtNotificationSender {
    override suspend fun showDreamArtComplete(imageUrl: String, previewImageBytes: ByteArray?) {
        showNotification(
            identifier = "dream_art_complete_notification",
            title = "Your dream art is ready",
            body = "Your generated dream image has been saved.",
            imageUrl = imageUrl,
            previewImageBytes = previewImageBytes
        )
    }

    override suspend fun showDreamWorldComplete(imageUrl: String, previewImageBytes: ByteArray?) {
        showNotification(
            identifier = "dream_world_complete_notification",
            title = "Your dream world is ready",
            body = "Your new world painting has been saved.",
            imageUrl = imageUrl,
            previewImageBytes = previewImageBytes
        )
    }

    private suspend fun showNotification(
        identifier: String,
        title: String,
        body: String,
        imageUrl: String,
        previewImageBytes: ByteArray?,
    ) {
        if (!IosNotificationAuthorization.requestAuthorizationIfNeeded(identifier)) return

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            createAttachment(identifier, imageUrl, previewImageBytes)?.let { attachment ->
                setAttachments(listOf(attachment))
            }
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 1.0,
            repeats = false
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to send generated art notification $identifier: $error")
                }
            }
    }

    private suspend fun createAttachment(
        identifier: String,
        imageUrl: String,
        previewImageBytes: ByteArray?,
    ): UNNotificationAttachment? {
        val bytes = try {
            previewImageBytes ?: when {
                imageUrl.startsWith("data:image") -> imageUrl.substringAfter("base64,").decodeBase64Bytes()
                imageUrl.isNotBlank() -> downloadImageBytes(imageUrl)
                else -> null
            }
        } catch (e: Exception) {
            println("Unable to load generated art notification attachment: $e")
            null
        } ?: return null

        val url = writeAttachmentFile(bytes) ?: return null
        return UNNotificationAttachment.attachmentWithIdentifier(
            identifier = identifier,
            URL = url,
            options = mapOf(
                AttachmentTypeHintKey to PngTypeIdentifier
            ),
            error = null
        )
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun writeAttachmentFile(bytes: ByteArray): NSURL? {
        val fileName = "generated-art-${NSUUID.UUID().UUIDString}.png"
        val filePath = NSTemporaryDirectory() + fileName
        val data = bytes.usePinned {
            NSData.create(bytesNoCopy = it.addressOf(0), length = bytes.size.toULong())
        }
        return if (data.writeToFile(filePath, true)) {
            NSURL.fileURLWithPath(filePath)
        } else {
            null
        }
    }

    private companion object {
        const val AttachmentTypeHintKey = "UNNotificationAttachmentOptionsTypeHintKey"
        const val PngTypeIdentifier = "public.png"
    }
}
