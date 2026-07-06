package org.ballistic.dreamjournalai.shared.dream_notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultDailyTokenReminderTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultDreamJournalReminderTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultRealityCheckReminderTimes
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.FreeRealityCheckReminderLimit
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.MaxRealityCheckReminders
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationEvent
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationSettingsRepository

class NotificationScreenViewModel(
    private val settingsRepository: NotificationSettingsRepository,
    private val dailyTokenReminderScheduler: DailyTokenReminderScheduler,
    private val dreamReminderScheduler: DreamReminderScheduler,
) : ViewModel() {
    private val _notificationScreenState = MutableStateFlow(NotificationScreenState())
    val notificationScreenState: StateFlow<NotificationScreenState> = _notificationScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.dailyTokenReminderFlow.collectLatest { enabled ->
                _notificationScreenState.update { it.copy(dailyTokenReminderEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            settingsRepository.dailyTokenReminderTimeFlow.collectLatest { time ->
                _notificationScreenState.update { it.copy(dailyTokenReminderTime = time) }
            }
        }

        viewModelScope.launch {
            settingsRepository.dreamJournalReminderFlow.collectLatest { enabled ->
                _notificationScreenState.update { it.copy(dreamJournalReminderEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            settingsRepository.reminderTimeFlow.collectLatest { time ->
                _notificationScreenState.update { it.copy(dreamJournalReminderTime = time) }
            }
        }

        viewModelScope.launch {
            settingsRepository.realityCheckReminderFlow.collectLatest { enabled ->
                _notificationScreenState.update { it.copy(realityCheckReminderEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            settingsRepository.lucidityFrequencyFlow.collectLatest { count ->
                _notificationScreenState.update {
                    it.copy(realityCheckReminderCount = count.coerceIn(1, MaxRealityCheckReminders))
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.realityCheckReminderTimesFlow.collectLatest { times ->
                _notificationScreenState.update {
                    it.copy(realityCheckReminderTimes = normalizedRealityCheckTimes(times))
                }
            }
        }
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.ToggleDailyTokenReminder -> {
                _notificationScreenState.update { it.copy(dailyTokenReminderEnabled = event.enabled) }
                viewModelScope.launch {
                    settingsRepository.updateDailyTokenReminder(event.enabled)
                    dailyTokenReminderScheduler.setDailyTokenReminderEnabled(
                        enabled = event.enabled,
                        reminderTime = _notificationScreenState.value.dailyTokenReminderTime
                    )
                }
            }

            is NotificationEvent.SetDailyTokenReminderTime -> {
                _notificationScreenState.update { it.copy(dailyTokenReminderTime = event.localTime) }
                viewModelScope.launch {
                    settingsRepository.updateDailyTokenReminderTime(event.localTime)
                    if (_notificationScreenState.value.dailyTokenReminderEnabled) {
                        dailyTokenReminderScheduler.setDailyTokenReminderEnabled(
                            enabled = true,
                            reminderTime = event.localTime
                        )
                    }
                }
            }

            is NotificationEvent.ToggleDreamJournalReminder -> {
                _notificationScreenState.update { it.copy(dreamJournalReminderEnabled = event.dreamReminder) }
                viewModelScope.launch {
                    settingsRepository.updateDreamJournalReminder(event.dreamReminder)
                    dreamReminderScheduler.setDreamJournalReminderEnabled(
                        enabled = event.dreamReminder,
                        reminderTime = _notificationScreenState.value.dreamJournalReminderTime
                    )
                }
            }

            is NotificationEvent.SetDreamJournalReminderTime,
            is NotificationEvent.SetReminderTime -> {
                val time = when (event) {
                    is NotificationEvent.SetDreamJournalReminderTime -> event.localTime
                    is NotificationEvent.SetReminderTime -> event.localTime
                    else -> DefaultDreamJournalReminderTime
                }
                _notificationScreenState.update { it.copy(dreamJournalReminderTime = time) }
                viewModelScope.launch {
                    settingsRepository.updateReminderTime(time)
                    if (_notificationScreenState.value.dreamJournalReminderEnabled) {
                        dreamReminderScheduler.setDreamJournalReminderEnabled(
                            enabled = true,
                            reminderTime = time
                        )
                    }
                }
            }

            is NotificationEvent.ToggleRealityCheckReminder -> {
                _notificationScreenState.update {
                    it.copy(realityCheckReminderEnabled = event.lucidityNotification)
                }
                viewModelScope.launch {
                    settingsRepository.updateRealityCheckReminder(event.lucidityNotification)
                    syncRealityCheckReminders()
                }
            }

            is NotificationEvent.SetRealityCheckReminderCount -> {
                val allowedMax = if (event.isPremium) MaxRealityCheckReminders else FreeRealityCheckReminderLimit
                val count = event.count.coerceIn(1, allowedMax)
                _notificationScreenState.update { it.copy(realityCheckReminderCount = count) }
                viewModelScope.launch {
                    settingsRepository.updateLucidityFrequency(count)
                    syncRealityCheckReminders()
                }
            }

            is NotificationEvent.SetRealityCheckReminderTime -> {
                if (event.index !in 0 until MaxRealityCheckReminders) return
                val nextTimes = _notificationScreenState.value.realityCheckReminderTimes
                    .toMutableList()
                    .apply { this[event.index] = event.localTime }
                _notificationScreenState.update { it.copy(realityCheckReminderTimes = nextTimes) }
                viewModelScope.launch {
                    settingsRepository.updateRealityCheckReminderTimes(nextTimes)
                    syncRealityCheckReminders()
                }
            }

            else -> Unit
        }
    }

    private suspend fun syncRealityCheckReminders() {
        val state = _notificationScreenState.value
        dreamReminderScheduler.setRealityCheckReminders(
            enabled = state.realityCheckReminderEnabled,
            reminderTimes = state.realityCheckReminderTimes.take(state.realityCheckReminderCount)
        )
    }

    private fun normalizedRealityCheckTimes(times: List<LocalTime>): List<LocalTime> {
        return (times + DefaultRealityCheckReminderTimes)
            .take(MaxRealityCheckReminders)
    }
}

data class NotificationScreenState(
    val dailyTokenReminderEnabled: Boolean = true,
    val dailyTokenReminderTime: LocalTime = DefaultDailyTokenReminderTime,
    val dreamJournalReminderEnabled: Boolean = false,
    val dreamJournalReminderTime: LocalTime = DefaultDreamJournalReminderTime,
    val realityCheckReminderEnabled: Boolean = false,
    val realityCheckReminderCount: Int = 1,
    val realityCheckReminderTimes: List<LocalTime> = DefaultRealityCheckReminderTimes,
)
