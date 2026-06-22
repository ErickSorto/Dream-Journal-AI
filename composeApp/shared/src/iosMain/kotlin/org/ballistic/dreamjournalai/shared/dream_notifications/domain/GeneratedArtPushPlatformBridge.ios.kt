package org.ballistic.dreamjournalai.shared.dream_notifications.domain

internal actual object GeneratedArtPushPlatformBridge {
    actual fun start(
        onNewToken: (String) -> Unit,
        onNotificationClick: (destination: String?, dreamId: String?) -> Unit
    ) = Unit

    actual suspend fun getToken(): String? = null
}
