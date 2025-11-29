package org.ballistic.dreamjournalai.shared

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed class BottomNavigationEvent {
    data class SetVisibility(val visible: Boolean) : BottomNavigationEvent()
    data class SetEnabled(val enabled: Boolean) : BottomNavigationEvent()
}

object BottomNavigationController {
    private val _events = Channel<BottomNavigationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: BottomNavigationEvent) {
        _events.send(event)
    }

    fun trySend(event: BottomNavigationEvent) {
        _events.trySend(event)
    }
}
