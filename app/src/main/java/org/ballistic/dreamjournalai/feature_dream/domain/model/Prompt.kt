package org.ballistic.dreamjournalai.feature_dream.domain.model

import org.ballistic.dreamjournalai.BuildConfig

data class Prompt(
    val api_key: String = BuildConfig.API_KEY,
    val model: String,
    val prompt: String,
    val max_tokens: Int,
    val temperature: Int,
    val frequency_penalty: Int
)


