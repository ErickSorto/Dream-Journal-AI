package org.ballistic.dreamjournalai.shared

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

sealed class DrawerCommand {
    data object Open : DrawerCommand()
    data object Close : DrawerCommand()
    data object Toggle : DrawerCommand()
}

object DrawerController {
    private val _events = Channel<DrawerCommand>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    suspend fun send(command: DrawerCommand) {
        if (_isEnabled.value) {
            _events.send(command)
        }
    }

    fun enable() {
        _isEnabled.value = true
    }

    fun disable() {
        _isEnabled.value = false
    }
}
