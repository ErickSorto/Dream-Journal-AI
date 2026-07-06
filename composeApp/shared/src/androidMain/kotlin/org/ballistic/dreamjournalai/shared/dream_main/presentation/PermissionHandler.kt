package org.ballistic.dreamjournalai.shared.dream_main.presentation

import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return NotificationPermissionState(
            isRequired = false,
            isGranted = true,
            requestPermission = {}
        )
    }

    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.POST_NOTIFICATIONS
    )
    val isGranted = permissionState.status.isGranted
    return NotificationPermissionState(
        isRequired = true,
        isGranted = isGranted,
        requestPermission = {
            if (!isGranted) {
                permissionState.launchPermissionRequest()
            }
        }
    )
}
