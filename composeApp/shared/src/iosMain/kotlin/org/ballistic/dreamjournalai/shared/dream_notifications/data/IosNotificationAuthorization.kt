package org.ballistic.dreamjournalai.shared.dream_notifications.data

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal object IosNotificationAuthorization {
    suspend fun requestAuthorizationIfNeeded(source: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val center = UNUserNotificationCenter.currentNotificationCenter()

            center.getNotificationSettingsWithCompletionHandler { settings ->
                when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized -> {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    UNAuthorizationStatusNotDetermined -> {
                        center.requestAuthorizationWithOptions(
                            UNAuthorizationOptionAlert or
                                UNAuthorizationOptionSound or
                                UNAuthorizationOptionBadge
                        ) { granted, error ->
                            if (error != null) {
                                println("Failed to request notification permission for $source: $error")
                            }
                            if (continuation.isActive) continuation.resume(granted)
                        }
                    }

                    else -> {
                        println(
                            "Notification permission is not granted for $source. " +
                                "Current iOS status: ${settings?.authorizationStatus}"
                        )
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
        }
}
