package org.ballistic.dreamjournalai.shared

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.ballistic.dreamjournalai.shared.core.util.StringValue

data class SnackbarEvent(
    val message: StringValue,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
)

data class SnackbarAction (
    val name: StringValue,
    val action: () -> Unit
)

object SnackbarController {
    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}
