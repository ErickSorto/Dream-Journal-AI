package org.ballistic.dreamjournalai.shared.core.platform

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
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
import java.io.File

class AndroidAudioRecorder(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    private var currentFile: File? = null
    private var amplitudeJob: Job? = null

    override fun start() {
        if (currentFile == null) {
            val cacheDir = context.cacheDir
            currentFile = File(cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            recorder = MediaRecorder()
        }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(24000) // 24 kbps
            setAudioChannels(1) // Mono
            setAudioSamplingRate(44100)
            setOutputFile(currentFile!!.absolutePath)
            try {
                prepare()
                start()
                startAmplitudePolling()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = coroutineScope.launch {
            while (isActive) {
                recorder?.maxAmplitude?.let { maxAmp ->
                    _amplitude.value = maxAmp.toFloat() / 32767f // Normalize 16-bit
                }
                delay(100)
            }
        }
    }

    override fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.pause()
                amplitudeJob?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.resume()
                startAmplitudePolling()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stop(): String {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            amplitudeJob?.cancel()
        }
        val path = currentFile?.absolutePath ?: ""
        currentFile = null
        return path
    }
}

@Composable
actual fun rememberAudioRecorder(): AudioRecorder {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context) { AndroidAudioRecorder(context, scope) }
}
