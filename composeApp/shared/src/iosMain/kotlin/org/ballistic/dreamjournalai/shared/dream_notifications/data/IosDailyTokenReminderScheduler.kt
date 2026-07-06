package org.ballistic.dreamjournalai.shared.dream_notifications.data

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import platform.Foundation.NSDateComponents
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class IosDailyTokenReminderScheduler : DailyTokenReminderScheduler {
    override suspend fun setDailyTokenReminderEnabled(enabled: Boolean, reminderTime: LocalTime) {
        defaults.setBool(enabled, KEY_ENABLED)
        defaults.setInteger(reminderTime.hour.toLong(), KEY_HOUR)
        defaults.setInteger(reminderTime.minute.toLong(), KEY_MINUTE)

        if (enabled) {
            scheduleNext(reminderTime)
        } else {
            cancel()
        }
    }

    override suspend fun syncDailyTokenReminder(hasFullyClaimedToday: Boolean, reminderTime: LocalTime) {
        if (hasFullyClaimedToday) {
            defaults.setObject(currentLocalDay(), KEY_LAST_CLAIM_DAY)
        } else {
            defaults.removeObjectForKey(KEY_LAST_CLAIM_DAY)
        }
        defaults.setInteger(reminderTime.hour.toLong(), KEY_HOUR)
        defaults.setInteger(reminderTime.minute.toLong(), KEY_MINUTE)

        if (isEnabled()) {
            scheduleNext(reminderTime)
        }
    }

    private suspend fun scheduleNext(reminderTime: LocalTime = storedReminderTime()) {
        cancel()

        if (!IosNotificationAuthorization.requestAuthorizationIfNeeded("daily token reminder")) {
            return
        }

        val next = nextReminderDateTime(reminderTime)
        val content = UNMutableNotificationContent().apply {
            setTitle("Claim your daily token")
            setBody("Your DreamNorth token is ready.")
            setSound(UNNotificationSound.defaultSound())
            setUserInfo(
                mapOf(
                    NotificationNavigationController.EXTRA_DESTINATION to
                        NotificationDestination.DailyTokens.rawValue
                )
            )
            notificationImageAttachment("daily_token_notification_attachment")?.let { attachment ->
                setAttachments(listOf(attachment))
            }
        }
        val components = NSDateComponents().apply {
            year = next.year.toLong()
            month = next.monthNumber.toLong()
            day = next.dayOfMonth.toLong()
            hour = next.hour.toLong()
            minute = next.minute.toLong()
            second = 0
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
                    println("Failed to schedule daily token reminder: $error")
                }
            }
    }

    private fun cancel() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
    }

    private fun nextReminderDateTime(reminderTime: LocalTime): LocalDateTime {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var next = LocalDateTime(
            year = now.year,
            month = now.month,
            dayOfMonth = now.dayOfMonth,
            hour = reminderTime.hour,
            minute = reminderTime.minute,
            second = 0,
            nanosecond = 0
        )

        if (next <= now || defaults.stringForKey(KEY_LAST_CLAIM_DAY) == currentLocalDay()) {
            val tomorrow = now.date.plus(DatePeriod(days = 1))
            next = LocalDateTime(
                year = tomorrow.year,
                month = tomorrow.month,
                dayOfMonth = tomorrow.dayOfMonth,
                hour = reminderTime.hour,
                minute = reminderTime.minute,
                second = 0,
                nanosecond = 0
            )
        }

        return next
    }

    private fun storedReminderTime(): LocalTime {
        return LocalTime(
            hour = defaults.integerForKey(KEY_HOUR).toInt().takeIf { it in 0..23 } ?: 8,
            minute = defaults.integerForKey(KEY_MINUTE).toInt().takeIf { it in 0..59 } ?: 0
        )
    }

    private fun isEnabled(): Boolean {
        return if (defaults.objectForKey(KEY_ENABLED) == null) {
            true
        } else {
            defaults.boolForKey(KEY_ENABLED)
        }
    }

    private fun currentLocalDay(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    companion object {
        private const val REQUEST_ID = "daily_token_reminder"
        private const val KEY_ENABLED = "daily_token_reminder_enabled"
        private const val KEY_HOUR = "daily_token_reminder_hour"
        private const val KEY_MINUTE = "daily_token_reminder_minute"
        private const val KEY_LAST_CLAIM_DAY = "daily_token_last_claim_day"
    }
}
