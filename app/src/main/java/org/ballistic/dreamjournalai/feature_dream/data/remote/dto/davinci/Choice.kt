package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci

import androidx.annotation.Keep

@Keep
data class Choice(
    val finish_reason: String,
    val index: Int,
    val logprobs: Any,
    val text: String
)