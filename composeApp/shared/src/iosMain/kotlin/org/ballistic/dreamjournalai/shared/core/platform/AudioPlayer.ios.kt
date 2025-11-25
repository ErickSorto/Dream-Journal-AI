package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFoundation.*
import platform.Foundation.NSURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSError

class IosAudioPlayer(
    private val scope: CoroutineScope
) : AudioPlayer {
    private var player: AVPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    override val progress: StateFlow<Float> = _progress.asStateFlow()

    private var progressJob: Job? = null

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun play(url: String) {
        _progress.value = 0f
        
        val nsUrl = if (url.startsWith("http")) {
            NSURL.URLWithString(url)
        } else {
            NSURL.fileURLWithPath(url)
        }

        if (nsUrl == null) {
            Logger.e("IosAudioPlayer") { "Invalid URL: $url" }
            return
        }
        
        val session = AVAudioSession.sharedInstance()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val success = session.setCategory(
                AVAudioSessionCategoryPlayback, 
                withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
                error = error.ptr
            )
            if (!success) {
                Logger.e("IosAudioPlayer") { "Failed to set audio session category: ${error.value?.localizedDescription}" }
            }
            
            session.setActive(true, error = error.ptr)
        }

        if (player == null) {
            player = AVPlayer(uRL = nsUrl)
        } else {
            player?.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = nsUrl))
        }
        
        player?.play()
        _isPlaying.value = true
        startProgressPolling()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    val playerItem = player?.currentItem
                    if (playerItem != null) {
                        val currentTime = player?.currentTime()
                        val duration = playerItem.duration
                        
                        val currentSec = CMTimeGetSeconds(currentTime!!)
                        val durationSec = CMTimeGetSeconds(duration)

                        if (!durationSec.isNaN() && durationSec > 0) {
                            val p = (currentSec / durationSec).toFloat()
                            _progress.value = p.coerceIn(0f, 1f)
                            
                            if (p >= 0.99f) {
                                _isPlaying.value = false
                                _progress.value = 1f 
                            }
                        }
                    }
                }
                delay(50)
            }
        }
    }

    override fun pause() {
        player?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    override fun stop() {
        player?.pause()
        player?.replaceCurrentItemWithPlayerItem(null)
        _isPlaying.value = false
        _progress.value = 0f
        progressJob?.cancel()
    }

    override fun release() {
        stop()
        player = null
    }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    val scope = rememberCoroutineScope()
    val player = remember { IosAudioPlayer(scope) }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    return player
}
