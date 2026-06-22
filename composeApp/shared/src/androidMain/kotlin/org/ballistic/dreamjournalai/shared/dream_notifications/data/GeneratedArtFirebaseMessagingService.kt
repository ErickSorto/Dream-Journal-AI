package org.ballistic.dreamjournalai.shared.dream_notifications.data

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import kotlin.time.ExperimentalTime

class GeneratedArtFirebaseMessagingService : FirebaseMessagingService() {
    @OptIn(ExperimentalTime::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        runBlocking {
            val trimmed = token.trim()
            val uid = Firebase.auth.currentUser?.uid ?: return@runBlocking
            if (trimmed.isBlank()) return@runBlocking

            Firebase.firestore
                .collection("users")
                .document(uid)
                .collection("device_tokens")
                .document(trimmed.replace("/", "_").take(160))
                .set(
                    mapOf(
                        "token" to trimmed,
                        "active" to true,
                        "platform" to "android",
                        "updatedAt" to kotlin.time.Clock.System.now().toEpochMilliseconds()
                    )
                )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val imageUrl = data[KEY_IMAGE_URL].orEmpty()
        val type = data[KEY_TYPE].orEmpty()
        val isGeneratedArtPush = imageUrl.isNotBlank() &&
            (type == TYPE_DREAM_IMAGE || type == TYPE_DREAM_WORLD)

        if (!isGeneratedArtPush) {
            showFallbackNotification(message)
            return
        }

        val isWorld = type == TYPE_DREAM_WORLD
        val title = data[KEY_TITLE]
            ?: message.notification?.title
            ?: if (isWorld) "Your dream world is ready" else "Your dream art is ready"
        val body = data[KEY_BODY]
            ?: message.notification?.body
            ?: if (isWorld) "Your new world painting has been saved." else "Your generated dream image has been saved."
        val destination = data[NotificationNavigationController.EXTRA_DESTINATION]
        val dreamId = data[NotificationNavigationController.EXTRA_DREAM_ID]

        runBlocking {
            AndroidGeneratedArtNotificationSender(applicationContext).showGeneratedArtPush(
                title = title,
                message = body,
                imageUrl = imageUrl,
                destination = destination,
                dreamId = dreamId,
                isWorld = isWorld
            )
        }
    }

    private fun showFallbackNotification(message: RemoteMessage) {
        val notification = message.notification ?: return
        val title = notification.title ?: return
        val body = notification.body.orEmpty()

        runBlocking {
            AndroidGeneratedArtNotificationSender(applicationContext).showGeneratedArtPush(
                title = title,
                message = body,
                imageUrl = "",
                destination = message.data[NotificationNavigationController.EXTRA_DESTINATION],
                dreamId = message.data[NotificationNavigationController.EXTRA_DREAM_ID],
                isWorld = false
            )
        }
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_TYPE = "type"
        const val TYPE_DREAM_IMAGE = "dream_image"
        const val TYPE_DREAM_WORLD = "dream_world"
    }
}
