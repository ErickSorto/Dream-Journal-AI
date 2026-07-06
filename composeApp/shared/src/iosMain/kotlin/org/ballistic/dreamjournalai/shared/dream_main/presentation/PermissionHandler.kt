package org.ballistic.dreamjournalai.shared.dream_main.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    var isGranted by remember { mutableStateOf(true) }
    var isRequired by remember { mutableStateOf(false) }
    val center = remember { UNUserNotificationCenter.currentNotificationCenter() }

    LaunchedEffect(Unit) {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            isGranted = status == UNAuthorizationStatusAuthorized ||
                status == UNAuthorizationStatusProvisional
            isRequired = status == UNAuthorizationStatusNotDetermined || !isGranted
        }
    }

    return NotificationPermissionState(
        isRequired = isRequired,
        isGranted = isGranted,
        requestPermission = {
            center.getNotificationSettingsWithCompletionHandler { settings ->
                when (settings?.authorizationStatus) {
                    UNAuthorizationStatusNotDetermined -> {
                        center.requestAuthorizationWithOptions(
                            (platform.UserNotifications.UNAuthorizationOptionAlert or
                                platform.UserNotifications.UNAuthorizationOptionSound or
                                platform.UserNotifications.UNAuthorizationOptionBadge)
                        ) { granted, _ ->
                            isGranted = granted
                            isRequired = !granted
                        }
                    }
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional -> {
                        isGranted = true
                        isRequired = false
                    }
                    else -> {
                        isGranted = false
                        isRequired = true
                        val url = NSURL(string = UIApplicationOpenSettingsURLString)
                        UIApplication.sharedApplication.openURL(url)
                    }
                }
            }
        }
    )
}
