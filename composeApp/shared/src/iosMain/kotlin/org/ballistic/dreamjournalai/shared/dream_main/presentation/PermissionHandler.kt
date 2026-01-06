package org.ballistic.dreamjournalai.shared.dream_main.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun RequestNotificationPermission() {
    LaunchedEffect(Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusNotDetermined -> {
                    center.requestAuthorizationWithOptions(
                        (platform.UserNotifications.UNAuthorizationOptionAlert or
                                platform.UserNotifications.UNAuthorizationOptionSound or
                                platform.UserNotifications.UNAuthorizationOptionBadge)
                    ) { granted, error ->
                        if (granted) {
                            // User granted permission
                        } else {
                            // User denied permission
                        }
                    }
                }
                UNAuthorizationStatusAuthorized -> {
                    // Permission already granted
                }
                else -> {
                    // Permission denied or restricted, open app settings
                    val url = NSURL(string = UIApplicationOpenSettingsURLString)
                    UIApplication.sharedApplication.openURL(url)
                }
            }
        }
    }
}