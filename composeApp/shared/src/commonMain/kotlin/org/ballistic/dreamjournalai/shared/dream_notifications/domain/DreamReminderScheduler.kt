package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import kotlinx.datetime.LocalTime

const val MaxRealityCheckReminders = 3
const val FreeRealityCheckReminderLimit = 2

val DefaultDreamJournalReminderTime = LocalTime(hour = 7, minute = 0)
val DefaultRealityCheckReminderTimes = listOf(
    LocalTime(hour = 10, minute = 0),
    LocalTime(hour = 15, minute = 0),
    LocalTime(hour = 20, minute = 0),
)

interface DreamReminderScheduler {
    suspend fun setDreamJournalReminderEnabled(enabled: Boolean, reminderTime: LocalTime)
    suspend fun setRealityCheckReminders(enabled: Boolean, reminderTimes: List<LocalTime>)
}
