package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat

import androidx.annotation.Keep

@Keep
data class Message(
    val role: String,
    val content: String
)