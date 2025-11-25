package org.ballistic.dreamjournalai.shared.core.platform

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class AndroidAudioPlayer(
    context: Context,
    private val scope: CoroutineScope
) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    override val progress: StateFlow<Float> = _progress.asStateFlow()

    private var progressJob: Job? = null

    override fun play(url: String) {
        // Reset progress when starting fresh
        _progress.value = 0f
        
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    _isPlaying.value = false
                    _progress.value = 1f // Ensure it looks finished
                    stopProgressPolling()
                }
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    startProgressPolling()
                }
            }
        } else {
            mediaPlayer?.reset()
        }

        try {
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val current = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 1
                if (duration > 0) {
                    _progress.value = current.toFloat() / duration.toFloat()
                }
                delay(50) // 20fps update
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
    }

    override fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                stopProgressPolling()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Optional resume helper if needed internally or externally (not in interface currently but useful)
    fun resume() {
        try {
            if (mediaPlayer != null && !_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressPolling()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            _isPlaying.value = false
            _progress.value = 0f
            stopProgressPolling()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        stopProgressPolling()
    }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember(context) { AndroidAudioPlayer(context, scope) }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    return player
}
