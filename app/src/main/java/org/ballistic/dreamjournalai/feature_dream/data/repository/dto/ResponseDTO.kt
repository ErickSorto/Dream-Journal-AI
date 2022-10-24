package org.ballistic.dreamjournalai.feature_dream.data.repository.dto

data class ResponseDTO(
    val logprobs: Any,
    val max_tokens: Int,
    val model: String,
    val n: Int,
    val prompt: String,
    val stop: String,
    val stream: Boolean,
    val temperature: Int,
    val top_p: Int
)