package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NotificationDestination(val rawValue: String) {
    DailyTokens("daily_tokens"),
    DreamJournal("dream_journal"),
    RealityCheck("reality_check"),
    PaintDreamWorld("paint_dream_world"),
    Store("store");

    companion object {
        fun fromRawValue(value: String?): NotificationDestination? {
            return entries.firstOrNull { it.rawValue == value }
        }
    }
}

data class NotificationNavigationRequest(
    val destination: NotificationDestination,
    val dreamId: String = ""
)

object NotificationNavigationController {
    const val EXTRA_DESTINATION = "dreamnorth.notification.destination"
    const val EXTRA_DREAM_ID = "dreamnorth.notification.dream_id"

    private val _destinations = MutableStateFlow<NotificationNavigationRequest?>(null)

    val destinations: StateFlow<NotificationNavigationRequest?> = _destinations.asStateFlow()

    fun open(destination: NotificationDestination, dreamId: String = "") {
        _destinations.value = NotificationNavigationRequest(destination, dreamId)
    }

    fun open(rawDestination: String?) {
        open(rawDestination, "")
    }

    fun open(rawDestination: String?, dreamId: String?) {
        val destination = NotificationDestination.fromRawValue(rawDestination) ?: return
        open(destination, dreamId.orEmpty())
    }

    fun openRawDestination(rawDestination: String?) {
        open(rawDestination)
    }

    fun openRawDestination(rawDestination: String?, dreamId: String?) {
        open(rawDestination, dreamId)
    }

    fun clear(request: NotificationNavigationRequest) {
        if (_destinations.value == request) {
            _destinations.value = null
        }
    }
}
