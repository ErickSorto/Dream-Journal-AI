package org.ballistic.dreamjournalai.shared.dream_notifications.domain

interface GeneratedArtNotificationSender {
    suspend fun showDreamArtComplete(imageUrl: String, previewImageBytes: ByteArray? = null)
    suspend fun showDreamWorldComplete(imageUrl: String, previewImageBytes: ByteArray? = null)
}
