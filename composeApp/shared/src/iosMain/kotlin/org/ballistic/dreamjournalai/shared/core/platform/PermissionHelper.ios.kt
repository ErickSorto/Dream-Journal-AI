package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberRecordAudioPermissionState(): PermissionState {
    var isGranted by remember {
        mutableStateOf(
            AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
        )
    }

    return remember(isGranted) {
        object : PermissionState {
            override val isGranted: Boolean = isGranted
            override fun launchRequest() {
                AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        isGranted = granted
                    }
                }
            }
        }
    }
}
