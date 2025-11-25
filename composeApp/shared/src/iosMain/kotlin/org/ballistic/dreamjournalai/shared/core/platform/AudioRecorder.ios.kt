package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import co.touchlab.kermit.Logger
import kotlinx.cinterop.*
import platform.AVFAudio.*
import platform.Foundation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC

@OptIn(ExperimentalForeignApi::class)
class IosAudioRecorder(
    private val coroutineScope: CoroutineScope
) : AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    private var amplitudeJob: Job? = null
    private var currentUrl: NSURL? = null

    override fun start() {
        val session = AVAudioSession.sharedInstance()
        
        // Check permissions first
        val permission = session.recordPermission()
        Logger.d("IosAudioRecorder") { "Record permission status: $permission" }
        
        if (permission != AVAudioSessionRecordPermissionGranted) {
            Logger.e("IosAudioRecorder") { "Record permission not granted ($permission). Cannot start recording." }
            // If not granted, we can't record. The UI should have handled this, but we guard here.
            return
        }

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            
            // Deactivate first to ensure clean state (optional but helpful)
            session.setActive(false, error = null)

            // Set category
            val categorySuccess = session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionAllowBluetooth,
                error = error.ptr
            )
            
            if (!categorySuccess) {
                Logger.e("IosAudioRecorder") { "Failed to set session category: ${error.value?.localizedDescription}" }
                return
            }

            // Set active
            val activeSuccess = session.setActive(true, error = error.ptr)
            if (!activeSuccess) {
                Logger.e("IosAudioRecorder") { "Failed to set session active: ${error.value?.localizedDescription}" }
                return
            }
        }

        val fileName = "recording_${NSDate().timeIntervalSince1970}.m4a"
        val tempDir = NSTemporaryDirectory()
        val path = tempDir + fileName
        val url = NSURL.fileURLWithPath(path)
        currentUrl = url
        
        Logger.d("IosAudioRecorder") { "Initializing recorder at path: ${url.absoluteString}" }

        // Use standard settings.
        // Note: Keys are strings, values are NSNumbers (bridged from Int/Double)
        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to kAudioFormatMPEG4AAC.toInt(),
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderBitRateKey to 64000, // Increased slightly for quality/stability
            AVEncoderAudioQualityKey to AVAudioQualityHigh
        )

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val newRecorder = AVAudioRecorder(url, settings, error.ptr)
            
            if (newRecorder == null) {
                Logger.e("IosAudioRecorder") { "Failed to create AVAudioRecorder: ${error.value?.localizedDescription}" }
                return
            }
            
            recorder = newRecorder
            newRecorder.meteringEnabled = true
            
            if (newRecorder.prepareToRecord()) {
                if (newRecorder.record()) {
                    Logger.d("IosAudioRecorder") { "AVAudioRecorder started recording successfully" }
                    startAmplitudePolling()
                } else {
                    Logger.e("IosAudioRecorder") { "AVAudioRecorder.record() returned false. Check permissions or audio session." }
                }
            } else {
                Logger.e("IosAudioRecorder") { "AVAudioRecorder.prepareToRecord() returned false" }
            }
        }
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = coroutineScope.launch {
            while (isActive) {
                recorder?.updateMeters()
                val power = recorder?.averagePowerForChannel(0u) ?: -160f
                // Typical range is -160 (silence) to 0 (max)
                // Normalize -60dB to 0dB for visualizer
                val minDb = -60f
                val normalized = ((power - minDb) / (0f - minDb)).coerceIn(0f, 1f)
                _amplitude.value = normalized
                delay(50)
            }
        }
    }

    override fun pause() {
        recorder?.pause()
        amplitudeJob?.cancel()
    }

    override fun resume() {
        recorder?.record()
        startAmplitudePolling()
    }

    override fun stop(): String {
        Logger.d("IosAudioRecorder") { "Stopping recorder" }
        recorder?.stop()
        amplitudeJob?.cancel()
        recorder = null
        
        // Deactivate session to allow other audio to resume if needed
        val session = AVAudioSession.sharedInstance()
        session.setActive(false, error = null)
        
        return currentUrl?.path ?: ""
    }
}

@Composable
actual fun rememberAudioRecorder(): AudioRecorder {
    val scope = rememberCoroutineScope()
    return remember { IosAudioRecorder(scope) }
}
