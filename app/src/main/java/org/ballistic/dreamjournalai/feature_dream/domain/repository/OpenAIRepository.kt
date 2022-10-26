package org.ballistic.dreamjournalai.feature_dream.domain.repository

import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO

interface OpenAIRepository {
    suspend fun getCompletion(
        model: String, prompt: String,
        maxTokens: Int, temperature: Int, frequencyPenalty: Int
    ): CompletionDTO
}