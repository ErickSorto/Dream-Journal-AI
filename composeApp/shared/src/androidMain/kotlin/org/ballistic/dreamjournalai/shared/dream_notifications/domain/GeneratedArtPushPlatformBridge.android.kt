package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.push.PushListener
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import com.mmk.kmpnotifier.push.firebase.addPushListener

internal actual object GeneratedArtPushPlatformBridge {
    actual fun start(
        onNewToken: (String) -> Unit,
        onNotificationClick: (destination: String?, dreamId: String?) -> Unit
    ) {
        KMPNotifier.addPushListener(object : PushListener {
            override fun onNewToken(token: String) {
                onNewToken(token)
            }
        })
        KMPNotifier.addListener(object : KMPNotifier.Listener {
            override fun onNotificationClicked(data: Map<String, Any?>) {
                val destination = data[NotificationNavigationController.EXTRA_DESTINATION] as? String
                val dreamId = data[NotificationNavigationController.EXTRA_DREAM_ID] as? String
                onNotificationClick(destination, dreamId)
            }
        })
    }

    actual suspend fun getToken(): String? {
        return FirebasePush.notifier.getToken()
    }
}
