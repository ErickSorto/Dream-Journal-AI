package org.ballistic.dreamjournalai.shared.dream_notifications.domain

interface PremiumTrialReminderScheduler {
    suspend fun scheduleTrialEndingReminder(triggerAtEpochMillis: Long)
    suspend fun cancelTrialEndingReminder()
}
