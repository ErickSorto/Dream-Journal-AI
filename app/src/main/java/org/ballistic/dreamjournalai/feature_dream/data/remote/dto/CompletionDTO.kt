package org.ballistic.dreamjournalai.feature_dream.data.remote.dto

data class CompletionDTO(
    val choices: List<Choice>,
    val created: Int,
    val id: String,
    val model: String,
    val `object`: String,
    val usage: Usage
)