package org.ballistic.dreamjournalai.shared.dream_debug.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_debug.domain.DebugNotificationTester

class DebugToolsViewModel(
    private val notificationTester: DebugNotificationTester,
) : ViewModel() {
    private val _state = MutableStateFlow(DebugToolsScreenState())
    val state: StateFlow<DebugToolsScreenState> = _state.asStateFlow()

    fun onEvent(event: DebugToolsEvent) {
        when (event) {
            DebugToolsEvent.TestDreamTokenNotification -> testNotification(
                label = "Dream token notification sent",
                action = notificationTester::showDreamTokenNotification
            )

            DebugToolsEvent.TestDreamJournalNotification -> testNotification(
                label = "Dream journal notification sent",
                action = notificationTester::showDreamJournalNotification
            )

            DebugToolsEvent.TestRealityCheckNotification -> testNotification(
                label = "Reality checker notification sent",
                action = notificationTester::showRealityCheckNotification
            )
        }
    }

    private fun testNotification(
        label: String,
        action: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(lastAction = "Sending...") }
            runCatching { action() }
                .onSuccess { _state.update { it.copy(lastAction = label) } }
                .onFailure { error ->
                    _state.update { it.copy(lastAction = error.message ?: "Notification test failed") }
                }
        }
    }
}

data class DebugToolsScreenState(
    val lastAction: String? = null,
)

sealed class DebugToolsEvent {
    data object TestDreamTokenNotification : DebugToolsEvent()
    data object TestDreamJournalNotification : DebugToolsEvent()
    data object TestRealityCheckNotification : DebugToolsEvent()
}
