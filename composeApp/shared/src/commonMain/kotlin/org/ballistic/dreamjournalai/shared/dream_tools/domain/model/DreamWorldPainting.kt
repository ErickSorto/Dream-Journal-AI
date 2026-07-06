package org.ballistic.dreamjournalai.shared.dream_tools.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DreamWorldPainting(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val description: String = "", // The ~30 word summary
    val timestamp: Long = 0L,
    val date: String = "",
    val status: String = "",
    val jobId: String = "",
    val errorCode: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long = 0L
)
