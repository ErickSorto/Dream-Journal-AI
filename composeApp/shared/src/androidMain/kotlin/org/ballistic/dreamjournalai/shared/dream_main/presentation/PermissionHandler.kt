package org.ballistic.dreamjournalai.shared.dream_main.presentation

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            permission = android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (!permissionState.status.isGranted) {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}