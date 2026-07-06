package org.ballistic.dreamjournalai.shared.dream_debug.data

import org.ballistic.dreamjournalai.shared.dream_debug.domain.DebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_notifications.data.IosNotificationAuthorization
import org.ballistic.dreamjournalai.shared.dream_notifications.data.notificationImageAttachment
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IosDebugNotificationTester : DebugNotificationTester {
    override suspend fun showDreamTokenNotification() {
        showNotification(
            identifier = "debug_dream_token_notification",
            title = "Claim your daily token",
            body = "Your DreamNorth token is ready.",
                attachmentName = "daily_token_notification_attachment",
            destination = NotificationDestination.DailyTokens
        )
    }

    override suspend fun showDreamJournalNotification() {
        showNotification(
            identifier = "debug_dream_journal_notification",
            title = "Write in your dream journal",
            body = "Take a minute to save what you remember.",
                attachmentName = "dream_journal_notification_attachment",
            destination = NotificationDestination.DreamJournal
        )
    }

    override suspend fun showRealityCheckNotification() {
        showNotification(
            identifier = "debug_reality_check_notification",
            title = "Reality check",
            body = "Pause and ask: am I dreaming?",
                attachmentName = "reality_check_notification_attachment",
            destination = NotificationDestination.RealityCheck
        )
    }

    private suspend fun showNotification(
        identifier: String,
        title: String,
        body: String,
        attachmentName: String,
        destination: NotificationDestination,
    ) {
        if (!IosNotificationAuthorization.requestAuthorizationIfNeeded(identifier)) {
            throw IllegalStateException("Notification permission is not granted")
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setUserInfo(
                mapOf(
                    NotificationNavigationController.EXTRA_DESTINATION to destination.rawValue
                )
            )
            notificationImageAttachment(attachmentName)?.let { attachment ->
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
                    println("Failed to send debug notification $identifier: $error")
                }
            }
    }

}
