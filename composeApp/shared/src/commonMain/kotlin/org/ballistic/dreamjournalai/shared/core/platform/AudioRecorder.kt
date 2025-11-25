package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val amplitude: StateFlow<Float> // 0f to 1f
    fun start()
    fun pause()
    fun resume()
    fun stop(): String
}

@Composable
expect fun rememberAudioRecorder(): AudioRecorder
