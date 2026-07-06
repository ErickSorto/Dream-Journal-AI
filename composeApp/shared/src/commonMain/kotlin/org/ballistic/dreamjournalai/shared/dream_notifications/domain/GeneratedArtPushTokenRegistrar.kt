package org.ballistic.dreamjournalai.shared.dream_notifications.domain

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class GeneratedArtPushTokenRegistrar {
    private val logger = Logger.withTag("GeneratedArtPushToken")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true

        GeneratedArtPushPlatformBridge.start(
            onNewToken = { token ->
                scope.launch { registerToken(token) }
            },
            onNotificationClick = { destination, dreamId ->
                NotificationNavigationController.openRawDestination(destination, dreamId)
            }
        )

        scope.launch {
            runCatching {
                GeneratedArtPushPlatformBridge.getToken()?.let { registerToken(it) }
            }.onFailure { error ->
                logger.w(error) { "Unable to read current push token." }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun registerToken(token: String) {
        val trimmed = token.trim()
        if (trimmed.isBlank()) return
        val uid = Firebase.auth.currentUser?.uid ?: return
        val tokenId = trimmed.replace("/", "_").take(160)

        runCatching {
            Firebase.firestore
                .collection("users")
                .document(uid)
                .collection("device_tokens")
                .document(tokenId)
                .set(
                    mapOf(
                        "token" to trimmed,
                        "active" to true,
                        "updatedAt" to kotlin.time.Clock.System.now().toEpochMilliseconds()
                    )
                )
        }.onFailure { error ->
            logger.w(error) { "Unable to register generated art push token." }
        }
    }
}

internal expect object GeneratedArtPushPlatformBridge {
    fun start(
        onNewToken: (String) -> Unit,
        onNotificationClick: (destination: String?, dreamId: String?) -> Unit
    )

    suspend fun getToken(): String?
}
