package org.ballistic.dreamjournalai.shared.dream_tools.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DreamWorldPainting(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val description: String = "", // The ~30 word summary
    val timestamp: Long = 0L,
    val date: String = ""
)
