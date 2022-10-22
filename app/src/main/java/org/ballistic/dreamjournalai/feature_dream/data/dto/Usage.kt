package org.ballistic.dreamjournalai.feature_dream.data.dto

data class Usage(
    val completion_tokens: Int,
    val prompt_tokens: Int,
    val total_tokens: Int
)