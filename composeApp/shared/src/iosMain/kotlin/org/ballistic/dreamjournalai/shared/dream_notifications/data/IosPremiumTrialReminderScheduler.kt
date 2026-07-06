package org.ballistic.dreamjournalai.shared.dream_notifications.data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class IosPremiumTrialReminderScheduler : PremiumTrialReminderScheduler {
    override suspend fun scheduleTrialEndingReminder(triggerAtEpochMillis: Long) {
        cancelTrialEndingReminder()

        if (!IosNotificationAuthorization.requestAuthorizationIfNeeded("premium trial reminder")) {
            return
        }

        val triggerAt = Instant.fromEpochMilliseconds(triggerAtEpochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val content = UNMutableNotificationContent().apply {
            setTitle("Your free trial ends soon")
            setBody("DreamNorth Premium renews in 2 days. You can manage or cancel anytime.")
            setSound(UNNotificationSound.defaultSound())
            setUserInfo(
                mapOf(
                    NotificationNavigationController.EXTRA_DESTINATION to NotificationDestination.Store.rawValue
                )
            )
            notificationImageAttachment("dream_journal_notification_attachment")?.let { attachment ->
                setAttachments(listOf(attachment))
            }
        }
        val components = NSDateComponents().apply {
            year = triggerAt.year.toLong()
            month = (triggerAt.month.ordinal + 1).toLong()
            day = triggerAt.day.toLong()
            hour = triggerAt.hour.toLong()
            minute = triggerAt.minute.toLong()
            second = triggerAt.second.toLong()
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = false
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = REQUEST_ID,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule premium trial reminder: $error")
                }
            }
    }

    override suspend fun cancelTrialEndingReminder() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
    }

    companion object {
        private const val REQUEST_ID = "premium_trial_ending_reminder"
    }
}
