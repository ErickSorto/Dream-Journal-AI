package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat

import androidx.annotation.Keep

@Keep
data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)