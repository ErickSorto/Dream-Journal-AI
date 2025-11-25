package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val progress: StateFlow<Float> // 0f to 1f
    fun play(url: String)
    fun pause()
    fun stop()
    fun release()
}

@Composable
expect fun rememberAudioPlayer(): AudioPlayer
