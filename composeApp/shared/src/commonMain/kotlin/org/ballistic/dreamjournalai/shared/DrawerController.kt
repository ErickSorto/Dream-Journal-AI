package org.ballistic.dreamjournalai.shared

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed class DrawerCommand {
    data object Open : DrawerCommand()
    data object Close : DrawerCommand()
    data object Toggle : DrawerCommand()
}

object DrawerController {
    private val _events = Channel<DrawerCommand>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun send(command: DrawerCommand) {
        _events.send(command)
    }
}

