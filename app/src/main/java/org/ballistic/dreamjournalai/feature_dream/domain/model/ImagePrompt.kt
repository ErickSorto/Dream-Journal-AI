package org.ballistic.dreamjournalai.feature_dream.domain.model

data class ImagePrompt(
    val prompt: String,
    val n: Int,
    val size: String,
    val model: String = "dall-e-3", // Assuming "dall-e-3" as default
    val quality: String = "standard" // Assuming "standard" as default
)
