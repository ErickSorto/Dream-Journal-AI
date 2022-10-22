package org.ballistic.dreamjournalai.feature_dream.data.dto

data class Choice(
    val finish_reason: String,
    val index: Int,
    val logprobs: Any,
    val text: String
)