package org.ballistic.dreamjournalai.shared.dream_notifications.data

import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.MaxRealityCheckReminders
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

class IosDreamReminderScheduler : DreamReminderScheduler {
    override suspend fun setDreamJournalReminderEnabled(enabled: Boolean, reminderTime: LocalTime) {
        cancel(listOf(DREAM_JOURNAL_REQUEST_ID))

        if (enabled) {
            if (IosNotificationAuthorization.requestAuthorizationIfNeeded("dream journal reminder")) {
                scheduleDaily(
                    requestId = DREAM_JOURNAL_REQUEST_ID,
                    reminderTime = reminderTime,
                    title = "Write in your dream journal",
                    body = "Take a minute to save what you remember.",
                    attachmentName = "dream_journal_notification_attachment",
                    destination = NotificationDestination.DreamJournal
                )
            }
        }
    }

    override suspend fun setRealityCheckReminders(enabled: Boolean, reminderTimes: List<LocalTime>) {
        cancel(realityRequestIds())

        if (!enabled) return
        if (!IosNotificationAuthorization.requestAuthorizationIfNeeded("reality check reminders")) return

        reminderTimes
            .take(MaxRealityCheckReminders)
            .forEachIndexed { index, reminderTime ->
                scheduleDaily(
                    requestId = realityRequestId(index),
                    reminderTime = reminderTime,
                    title = "Reality check",
                    body = "Pause and ask: am I dreaming?",
                    attachmentName = "reality_check_notification_attachment",
                    destination = NotificationDestination.RealityCheck
                )
            }
    }

    private fun scheduleDaily(
        requestId: String,
        reminderTime: LocalTime,
        title: String,
        body: String,
        attachmentName: String,
        destination: NotificationDestination,
    ) {
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
        val components = NSDateComponents().apply {
            hour = reminderTime.hour.toLong()
            minute = reminderTime.minute.toLong()
            second = 0
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = true
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = requestId,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule reminder $requestId: $error")
                }
            }
    }

    private fun cancel(requestIds: List<String>) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(requestIds)
    }

    private fun realityRequestIds(): List<String> {
        return List(MaxRealityCheckReminders) { index -> realityRequestId(index) }
    }

    private fun realityRequestId(index: Int): String = "$REALITY_CHECK_REQUEST_PREFIX$index"

    companion object {
        private const val DREAM_JOURNAL_REQUEST_ID = "dream_journal_daily_reminder"
        private const val REALITY_CHECK_REQUEST_PREFIX = "reality_check_daily_reminder_"
    }
}
