package org.ballistic.dreamjournalai.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun singleClick(
    lastClickTimeState: MutableState<Long>,
    onClick: () -> Unit
): () -> Unit {
    return {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastClickTimeState.value >= 300) {
            onClick()
            lastClickTimeState.value = now
        }
    }
}
