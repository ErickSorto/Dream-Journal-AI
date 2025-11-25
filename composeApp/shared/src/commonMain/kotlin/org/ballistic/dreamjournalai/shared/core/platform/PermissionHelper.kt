package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable

interface PermissionState {
    val isGranted: Boolean
    fun launchRequest()
}

@Composable
expect fun rememberRecordAudioPermissionState(): PermissionState
