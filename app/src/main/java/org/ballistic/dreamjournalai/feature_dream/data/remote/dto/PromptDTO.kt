package org.ballistic.dreamjournalai.feature_dream.data.remote.dto

import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt

data class PromptDTO(
    val logprobs: Any,
    val max_tokens: Int,
    val model: String,
    val n: Int,
    val prompt: String,
    val stop: String,
    val stream: Boolean,
    val temperature: Int,
    val top_p: Int,
    val frequency_penalty: Int
)

fun PromptDTO.toPrompt(): Prompt {
    return Prompt(
        max_tokens = max_tokens,
        model = model,
        prompt = prompt,
        temperature = temperature,
        frequency_penalty = frequency_penalty
    )
}