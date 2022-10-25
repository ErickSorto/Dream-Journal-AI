package org.ballistic.dreamjournalai.feature_dream.domain.model

data class Prompt(
    val max_tokens: Int,
    val model: String,
    val prompt: String,
    val temperature: Int,
    val frequency_penalty: Int
)


