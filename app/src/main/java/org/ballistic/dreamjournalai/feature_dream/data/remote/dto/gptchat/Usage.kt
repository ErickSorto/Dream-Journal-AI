package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)