package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import kotlinx.datetime.LocalTime


sealed class NotificationEvent {
    data class ToggleRealityCheckReminder(val lucidityNotification: Boolean) : NotificationEvent()
    data class ToggleDreamJournalReminder(val dreamReminder: Boolean) : NotificationEvent()
    data class ToggleDailyTokenReminder(val enabled: Boolean) : NotificationEvent()
    data class SetDailyTokenReminderTime(val localTime: LocalTime) : NotificationEvent()
    data class SetDreamJournalReminderTime(val localTime: LocalTime) : NotificationEvent()
    data class SetRealityCheckReminderCount(val count: Int, val isPremium: Boolean) : NotificationEvent()
    data class SetRealityCheckReminderTime(val index: Int, val localTime: LocalTime) : NotificationEvent()
    data class ChangeLucidityFrequency(val lucidityFrequency: Int) : NotificationEvent()
    data class SetReminderTime(val localTime: LocalTime) : NotificationEvent()
    data class SetTimeRange(val range: ClosedFloatingPointRange<Float>) : NotificationEvent()
    data object ScheduleLucidityNotification : NotificationEvent()
}
