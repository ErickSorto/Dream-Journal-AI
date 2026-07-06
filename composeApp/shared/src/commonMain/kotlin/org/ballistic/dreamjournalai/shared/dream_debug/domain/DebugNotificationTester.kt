package org.ballistic.dreamjournalai.shared.dream_debug.domain

interface DebugNotificationTester {
    suspend fun showDreamTokenNotification()
    suspend fun showDreamJournalNotification()
    suspend fun showRealityCheckNotification()
}
