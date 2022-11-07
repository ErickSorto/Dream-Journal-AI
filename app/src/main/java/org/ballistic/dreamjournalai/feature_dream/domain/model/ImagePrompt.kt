package org.ballistic.dreamjournalai.feature_dream.domain.model

data class ImagePrompt(
    val prompt: String,
    val n: Int,
    val size: String
)
