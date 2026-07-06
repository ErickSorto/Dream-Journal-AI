package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import kotlinx.datetime.LocalTime

interface DailyTokenReminderScheduler {
    suspend fun setDailyTokenReminderEnabled(enabled: Boolean, reminderTime: LocalTime)
    suspend fun syncDailyTokenReminder(hasFullyClaimedToday: Boolean, reminderTime: LocalTime)
}

val DefaultDailyTokenReminderTime = LocalTime(hour = 8, minute = 0)
